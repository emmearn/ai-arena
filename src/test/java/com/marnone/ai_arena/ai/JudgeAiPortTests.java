package com.marnone.ai_arena.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.JudgeRequest;
import com.marnone.ai_arena.domain.JudgeRubric;
import com.marnone.ai_arena.domain.JudgeVerdict;
import com.marnone.ai_arena.domain.Judgement;
import com.marnone.ai_arena.domain.MessageType;
import com.marnone.ai_arena.domain.Question;

class JudgeAiPortTests {

	@Test
	void judgePortIsSeparateFromSupervisorPort() {
		JudgeAiPort port = request -> new Judgement(
			JudgeVerdict.ACCEPT,
			new JudgeRubric(5, 5, 4, 4, 5, 5),
			"The answer is acceptable.",
			List.of()
		);

		Judgement judgement = port.judge(request());

		assertThat(judgement.verdict()).isEqualTo(JudgeVerdict.ACCEPT);
		assertThat(port).isNotInstanceOf(SupervisorAiPort.class);
	}

	private static JudgeRequest request() {
		return new JudgeRequest(
			new Question("How should AI Arena judge an answer?", Instant.EPOCH),
			List.of(new DebateMessage("message-1", "expert-1", 1, MessageType.PROPOSAL, "Initial point.", Instant.EPOCH)),
			new FinalAnswer("Final answer.", "Reasoned from debate.", "Converged."),
			"final-answer"
		);
	}
}
