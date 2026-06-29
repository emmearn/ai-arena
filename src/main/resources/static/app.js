const form = document.querySelector("#question-form");
const questionInput = document.querySelector("#question");
const questionError = document.querySelector("#question-error");
const submitButton = document.querySelector("#start-session");
const submitButtonLabel = submitButton?.querySelector(".button-label");
const sessionStatus = document.querySelector(".session-status");
const sessionStatusLabel = document.querySelector("#session-status-label");
const validationStatus = document.querySelector("#validation-status");

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
