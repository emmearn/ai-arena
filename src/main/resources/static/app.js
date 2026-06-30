const form = document.querySelector("#question-form");
const questionInput = document.querySelector("#question");
const questionError = document.querySelector("#question-error");
const submitButton = document.querySelector("#start-session");
const submitButtonLabel = submitButton?.querySelector(".button-label");
const sessionStatus = document.querySelector(".session-status");
const sessionStatusLabel = document.querySelector("#session-status-label");
const validationStatus = document.querySelector("#validation-status");
const teamStatus = document.querySelector("#team-status");
const expertList = document.querySelector("#expert-list");
const debateStatus = document.querySelector("#debate-status");
const messageList = document.querySelector("#message-list");
const finalPanel = document.querySelector(".final-panel");
const finalAnswer = document.querySelector("#final-answer");
const finalRationale = document.querySelector("#final-rationale");
const finalStopReason = document.querySelector("#final-stop-reason");
const finalJudgement = document.querySelector("#final-judgement");

const defaultTeamStatus = "Waiting for an accepted question.";
const defaultTeamEmptyState = "Accepted questions will assemble the expert team here.";
const defaultDebateStatus = "Waiting for the arena to start.";
const defaultDebateEmptyState = "Accepted questions will stream debate messages here.";
const defaultFinalAnswer = "The final response will appear here when the arena completes.";
const defaultFinalRationale = "Waiting for the debate outcome.";
const defaultFinalStopReason = "Not available yet.";
const defaultFinalJudgement = "Waiting for quality review.";
const expertsById = new Map();

const statusCopy = {
	idle: "Ready",
	validating: "Validating",
	accepted: "Accepted",
	rejected: "Rejected",
	error: "Error",
	running: "Running",
	complete: "Complete"
};

form?.addEventListener("submit", async (event) => {
	event.preventDefault();
	const question = questionInput.value.trim();

	clearFieldError();

	if (!question) {
		showFieldError("Enter a question before starting the arena.");
		setValidationState("rejected", "Question must not be empty.");
		return;
	}

	setBusy(true);
	setValidationState("validating", "Validating question...");
	resetTeamPanel();
	resetDebatePanel();
	resetFinalPanel();

	try {
		await streamArenaSession(question);
	}
	catch (error) {
		setValidationState("error", "Unable to start the arena session.");
	}
	finally {
		setBusy(false);
	}
});

questionInput?.addEventListener("input", () => {
	if (questionInput.value.trim()) {
		clearFieldError();
		if (validationStatus.dataset.state === "rejected") {
			setValidationState("idle", "Ready for a question.");
		}
	}
});

async function streamArenaSession(question) {
	const response = await fetch("/api/arena/sessions", {
		method: "POST",
		headers: {
			"Accept": "text/event-stream",
			"Content-Type": "application/json"
		},
		body: JSON.stringify({ question })
	});

	if (!response.ok || !response.body) {
		throw new Error("Arena session request failed.");
	}

	const reader = response.body.getReader();
	const decoder = new TextDecoder();
	let buffer = "";

	while (true) {
		const { value, done } = await reader.read();
		if (done) {
			break;
		}
		buffer += decoder.decode(value, { stream: true });
		const parts = buffer.split(/\r?\n\r?\n/);
		buffer = parts.pop() ?? "";
		parts.forEach(handleSseBlock);
	}

	if (buffer.trim()) {
		handleSseBlock(buffer);
	}
}

function handleSseBlock(block) {
	const lines = block.split(/\r?\n/);
	const eventLine = lines.find((line) => line.startsWith("event:"));
	const dataLines = lines.filter((line) => line.startsWith("data:"));
	const eventType = eventLine?.slice(6).trim();
	const payload = parsePayload(dataLines.map((line) => line.slice(5).trim()).join("\n"));

	if (eventType === "VALIDATION_STARTED") {
		setValidationState("validating", "Validating question...");
	}

	if (eventType === "VALIDATION_ACCEPTED") {
		setValidationState("accepted", "Question accepted. Building the arena...");
	}

	if (eventType === "VALIDATION_REJECTED") {
		setValidationState("rejected", payload.reason || "Question rejected.");
	}

	if (eventType === "TEAM_PLANNED") {
		setSessionState("running");
		validationStatus.dataset.state = "accepted";
		validationStatus.textContent = "Question accepted. Arena running...";
		renderTeamPlan(payload);
	}

	if (eventType === "EXPERT_CREATED") {
		renderExpert(payload);
	}

	if (eventType === "DEBATE_MESSAGE") {
		renderDebateMessage(payload);
	}

	if (eventType === "SUPERVISOR_DECISION") {
		renderSupervisorDecision(payload);
	}

	if (eventType === "JUDGEMENT") {
		renderJudgement(payload);
	}

	if (eventType === "FINAL_ANSWER") {
		renderFinalAnswer(payload);
	}

	if (eventType === "ERROR") {
		setValidationState("error", payload.message || "Unable to run arena session.");
		renderStreamError(payload);
	}
}

function parsePayload(raw) {
	if (!raw) {
		return {};
	}
	try {
		return JSON.parse(raw);
	}
	catch (error) {
		return {};
	}
}

function setValidationState(state, message) {
	setSessionState(state);
	validationStatus.dataset.state = state;
	validationStatus.textContent = message;
}

function setSessionState(state) {
	sessionStatus.dataset.state = state;
	sessionStatusLabel.textContent = statusCopy[state] || statusCopy.idle;
}

function resetTeamPanel() {
	expertsById.clear();
	teamStatus.textContent = defaultTeamStatus;
	expertList.replaceChildren(createEmptyState(defaultTeamEmptyState));
}

function resetDebatePanel() {
	debateStatus.textContent = defaultDebateStatus;
	messageList.replaceChildren(createEmptyState(defaultDebateEmptyState));
}

function resetFinalPanel() {
	finalPanel.dataset.state = "idle";
	finalAnswer.textContent = defaultFinalAnswer;
	finalRationale.textContent = defaultFinalRationale;
	finalStopReason.textContent = defaultFinalStopReason;
	finalJudgement.textContent = defaultFinalJudgement;
}

function renderTeamPlan(payload) {
	const count = Number(payload.expertCount) || 0;
	const roles = Array.isArray(payload.roles) ? payload.roles : [];
	const roleSummary = roles.length ? roles.join(", ") : "expert team";
	const countLabel = count === 1 ? "1 expert" : `${count} experts`;
	teamStatus.textContent = `Planning ${countLabel}: ${roleSummary}.`;
	expertList.replaceChildren(createEmptyState("Creating expert identities..."));
}

function renderExpert(payload) {
	if (!payload.name || !payload.role || !payload.mission) {
		return;
	}
	const expert = {
		id: payload.id || "",
		name: payload.name,
		role: payload.role,
		accent: safeAccent(payload.uiAccent)
	};
	expertsById.set(expert.id, expert);
	if (expertList.querySelector(".empty-state")) {
		expertList.replaceChildren();
	}

	const card = document.createElement("article");
	card.className = "expert-card";
	card.dataset.expertId = expert.id;
	card.style.setProperty("--accent", expert.accent);

	const avatar = document.createElement("div");
	avatar.className = "avatar";
	avatar.setAttribute("aria-hidden", "true");
	avatar.textContent = initialsFor(payload.name);

	const content = document.createElement("div");
	content.className = "expert-content";

	const headingRow = document.createElement("div");
	headingRow.className = "expert-heading";

	const name = document.createElement("h3");
	name.textContent = payload.name;

	const role = document.createElement("span");
	role.className = "role-badge";
	role.textContent = expert.role;

	headingRow.append(name, role);

	const mission = document.createElement("p");
	mission.textContent = payload.mission;

	const personality = document.createElement("p");
	personality.className = "expert-personality";
	personality.textContent = payload.personality || "professional and focused";

	content.append(headingRow, mission, personality);
	card.append(avatar, content);
	expertList.append(card);

	const createdCount = expertList.querySelectorAll(".expert-card").length;
	teamStatus.textContent = createdCount === 1 ? "1 expert ready." : `${createdCount} experts ready.`;
}

function renderDebateMessage(payload) {
	if (!payload.expertId || !payload.content) {
		return;
	}
	if (messageList.querySelector(".empty-state")) {
		messageList.replaceChildren();
	}

	const expert = expertsById.get(payload.expertId) || {
		name: "Arena expert",
		role: payload.expertId,
		accent: "#42bfd0"
	};
	const turn = Number(payload.turn) || messageList.querySelectorAll(".message-card").length + 1;

	clearActiveExpert();
	const activeExpert = expertList.querySelector(`[data-expert-id="${cssEscape(payload.expertId)}"]`);
	activeExpert?.classList.add("is-active");

	const card = document.createElement("article");
	card.className = "message-card";
	card.style.setProperty("--accent", expert.accent);
	card.dataset.expertId = payload.expertId;

	const meta = document.createElement("div");
	meta.className = "message-meta";

	const author = document.createElement("strong");
	author.textContent = expert.name;

	const detail = document.createElement("span");
	detail.textContent = `Turn ${turn} - ${formatMessageType(payload.messageType)}`;

	meta.append(author, detail);

	const role = document.createElement("p");
	role.className = "message-role";
	role.textContent = expert.role;

	const content = document.createElement("p");
	content.className = "message-content";
	content.textContent = payload.content;

	card.append(meta, role, content);
	messageList.append(card);
	debateStatus.textContent = `${expert.name} is speaking.`;
}

function renderSupervisorDecision(payload) {
	const reason = payload.reason || "Supervisor is deciding whether to continue.";
	debateStatus.textContent = reason;
	if (payload.action && String(payload.action).toUpperCase() !== "CONTINUE") {
		finalPanel.dataset.state = "deciding";
		finalStopReason.textContent = reason;
	}
}

function renderJudgement(payload) {
	const verdict = String(payload.verdict || "UNKNOWN").toUpperCase();
	const overall = payload.rubric?.overall ? `Overall ${payload.rubric.overall}/5` : "No score";
	const fallback = payload.fallbackApplied ? " - fallback review" : "";
	finalPanel.dataset.state = verdict === "REJECT" ? "error" : "deciding";
	finalJudgement.textContent = `${verdict} - ${overall}${fallback}. ${payload.reason || "Quality review completed."}`;
}

function renderFinalAnswer(payload) {
	finalPanel.dataset.state = "complete";
	clearActiveExpert();
	setSessionState("complete");
	validationStatus.dataset.state = "accepted";
	validationStatus.textContent = "Question accepted. Arena complete.";
	debateStatus.textContent = "Debate complete. Final answer ready.";
	finalAnswer.textContent = payload.content || "The arena completed without a final response.";
	finalRationale.textContent = payload.rationale || "The final answer was synthesized from the available debate.";
	finalStopReason.textContent = payload.stopReason || "The arena reached a controlled stop.";
}

function renderStreamError(payload) {
	finalPanel.dataset.state = "error";
	finalAnswer.textContent = "The arena could not complete this session.";
	finalRationale.textContent = payload.message || "A controlled error interrupted the run.";
	finalStopReason.textContent = "Stopped because an error occurred.";
}

function clearActiveExpert() {
	expertList.querySelectorAll(".expert-card.is-active").forEach((card) => {
		card.classList.remove("is-active");
	});
}

function createEmptyState(message) {
	const emptyState = document.createElement("p");
	emptyState.className = "empty-state";
	emptyState.textContent = message;
	return emptyState;
}

function initialsFor(value) {
	return value
		.split(/\s+/)
		.filter(Boolean)
		.slice(0, 2)
		.map((part) => part.charAt(0).toUpperCase())
		.join("");
}

function safeAccent(value) {
	return /^#[0-9a-f]{6}$/i.test(value || "") ? value : "#42bfd0";
}

function cssEscape(value) {
	if (window.CSS?.escape) {
		return window.CSS.escape(value);
	}
	return String(value).replaceAll("\"", "\\\"");
}

function formatMessageType(value) {
	return String(value || "message")
		.toLowerCase()
		.replaceAll("_", " ");
}

function showFieldError(message) {
	questionInput.setAttribute("aria-invalid", "true");
	questionError.textContent = message;
	questionInput.focus();
}

function clearFieldError() {
	questionInput.removeAttribute("aria-invalid");
	questionError.textContent = "";
}

function setBusy(isBusy) {
	submitButton.disabled = isBusy;
	submitButton.setAttribute("aria-busy", String(isBusy));
	submitButtonLabel.textContent = isBusy ? "Running" : "Start session";
}
