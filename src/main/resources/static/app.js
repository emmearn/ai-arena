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

const defaultTeamStatus = "Waiting for an accepted question.";
const defaultTeamEmptyState = "Accepted questions will assemble the expert team here.";

const statusCopy = {
	idle: "Ready",
	validating: "Validating",
	accepted: "Accepted",
	rejected: "Rejected",
	error: "Error",
	running: "Running"
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

	if (eventType === "ERROR") {
		setValidationState("error", payload.message || "Unable to run arena session.");
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
	teamStatus.textContent = defaultTeamStatus;
	expertList.replaceChildren(createEmptyState(defaultTeamEmptyState));
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
	if (expertList.querySelector(".empty-state")) {
		expertList.replaceChildren();
	}

	const card = document.createElement("article");
	card.className = "expert-card";
	card.dataset.expertId = payload.id || "";
	card.style.setProperty("--accent", safeAccent(payload.uiAccent));

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
	role.textContent = payload.role;

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
	return /^#[0-9a-f]{6}$/i.test(value || "") ? value : "#4cc9d8";
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
