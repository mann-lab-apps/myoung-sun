import {
  ArrowLeft,
  ArrowRight,
  CheckCircle2,
  Home,
  ListChecks,
  RotateCcw,
} from "lucide-react";
import { useMemo, useState } from "react";
import { tutorials, type Tutorial } from "./tutorialData";
import "./App.css";

const onboardingSteps = [
  {
    eyebrow: "1단계",
    title: "먼저 편하게 눌러봐요",
    body: "이 앱에서는 잘못 눌러도 큰일이 나지 않아요. 언제든 뒤로 가거나 다시 시작할 수 있습니다.",
    action: "알겠어요",
  },
  {
    eyebrow: "2단계",
    title: "한 번에 하나만 해요",
    body: "화면마다 해야 할 일을 하나씩 보여드릴게요. 읽고, 누르고, 다음으로 가면 됩니다.",
    action: "다음으로",
  },
  {
    eyebrow: "3단계",
    title: "AI에게 짧게 물어봐요",
    body: "처음에는 예시 문장을 그대로 따라 해도 됩니다. 익숙해지면 내 말로 조금씩 바꿔 볼 거예요.",
    action: "첫 연습 시작",
  },
];

type Screen = "home" | "onboarding" | "ready" | "tutorials" | "tutorial";

export function App() {
  const [screen, setScreen] = useState<Screen>("home");
  const [onboardingStepIndex, setOnboardingStepIndex] = useState(0);
  const [selectedTutorial, setSelectedTutorial] = useState<Tutorial>(tutorials[0]);
  const [tutorialStepIndex, setTutorialStepIndex] = useState(0);
  const [draft, setDraft] = useState("");
  const [selectedOption, setSelectedOption] = useState("");

  const currentStep = onboardingSteps[onboardingStepIndex];
  const isLastOnboardingStep = onboardingStepIndex === onboardingSteps.length - 1;
  const currentTutorialStep = selectedTutorial.steps[tutorialStepIndex];
  const isLastTutorialStep = tutorialStepIndex === selectedTutorial.steps.length - 1;

  const onboardingProgressText = useMemo(
    () => `${onboardingStepIndex + 1} / ${onboardingSteps.length}`,
    [onboardingStepIndex],
  );

  const tutorialProgressText = useMemo(
    () => `${tutorialStepIndex + 1} / ${selectedTutorial.steps.length}`,
    [selectedTutorial.steps.length, tutorialStepIndex],
  );

  const startOnboarding = () => {
    setOnboardingStepIndex(0);
    setScreen("onboarding");
  };

  const openTutorial = (tutorial: Tutorial) => {
    setSelectedTutorial(tutorial);
    setTutorialStepIndex(0);
    setDraft(tutorial.steps[0].prompt);
    setSelectedOption("");
    setScreen("tutorial");
  };

  const goNextOnboarding = () => {
    if (isLastOnboardingStep) {
      setScreen("ready");
      return;
    }

    setOnboardingStepIndex((index) => index + 1);
  };

  const goBackOnboarding = () => {
    if (onboardingStepIndex === 0) {
      setScreen("home");
      return;
    }

    setOnboardingStepIndex((index) => index - 1);
  };

  const goNextTutorial = () => {
    if (isLastTutorialStep) {
      setScreen("tutorials");
      return;
    }

    const nextIndex = tutorialStepIndex + 1;
    setTutorialStepIndex(nextIndex);
    setDraft(selectedTutorial.steps[nextIndex].prompt);
    setSelectedOption("");
  };

  const goBackTutorial = () => {
    if (tutorialStepIndex === 0) {
      setScreen("tutorials");
      return;
    }

    const previousIndex = tutorialStepIndex - 1;
    setTutorialStepIndex(previousIndex);
    setDraft(selectedTutorial.steps[previousIndex].prompt);
    setSelectedOption("");
  };

  return (
    <div className="app-shell">
      <header className="app-header" aria-label="앱 정보">
        <button className="brand-button" type="button" onClick={() => setScreen("home")}>
          <span className="brand-mark" aria-hidden="true">
            <Home size={24} strokeWidth={2.4} />
          </span>
          <span>명선</span>
        </button>
        <p>AI 질문 연습 앱</p>
      </header>

      <main>
        {screen === "home" ? (
          <section className="home" aria-labelledby="home-title">
            <div className="intro">
              <p className="eyebrow">삼성폰에서 바로 쓰는 연습</p>
              <h1 id="home-title">천천히 물어봐도 괜찮아요</h1>
              <p className="lead">
                AI에게 질문하는 일을 작은 단계로 나누어 연습합니다. 잘못 눌러도 다시 하면 되고,
                어려운 말은 쉬운 말로 바꿔 볼 수 있어요.
              </p>
              <div className="actions">
                <button className="primary-button" type="button" onClick={startOnboarding}>
                  시작하기
                  <ArrowRight size={24} aria-hidden="true" />
                </button>
                <button className="secondary-button" type="button" onClick={() => setScreen("tutorials")}>
                  바로 연습하기
                  <ListChecks size={22} aria-hidden="true" />
                </button>
              </div>
            </div>
          </section>
        ) : null}

        {screen === "onboarding" ? (
          <section className="onboarding" aria-labelledby="onboarding-title">
            <div className="step-panel">
              <div className="step-status" aria-label={`온보딩 ${onboardingProgressText}`}>
                <span>{currentStep.eyebrow}</span>
                <strong>{onboardingProgressText}</strong>
              </div>

              <h1 id="onboarding-title">{currentStep.title}</h1>
              <p className="lead">{currentStep.body}</p>

              <div className="step-actions">
                <button className="primary-button" type="button" onClick={goNextOnboarding}>
                  {currentStep.action}
                  {isLastOnboardingStep ? (
                    <CheckCircle2 size={24} aria-hidden="true" />
                  ) : (
                    <ArrowRight size={24} aria-hidden="true" />
                  )}
                </button>
                <button className="quiet-button" type="button" onClick={goBackOnboarding}>
                  <ArrowLeft size={22} aria-hidden="true" />
                  뒤로
                </button>
                <button className="quiet-button" type="button" onClick={() => setScreen("tutorials")}>
                  건너뛰기
                </button>
              </div>
            </div>
          </section>
        ) : null}

        {screen === "ready" ? (
          <section className="ready" aria-labelledby="ready-title">
            <div className="ready-panel">
              <CheckCircle2 className="ready-icon" size={48} aria-hidden="true" />
              <p className="eyebrow">준비 완료</p>
              <h1 id="ready-title">이제 첫 문장을 따라 해볼게요</h1>
              <p className="lead">
                예시 문장을 그대로 눌러 보고, 빈칸만 바꿔 보면서 AI 질문을 연습합니다.
              </p>
              <div className="actions">
                <button
                  className="primary-button"
                  type="button"
                  onClick={() => openTutorial(tutorials[0])}
                >
                  첫 연습으로 가기
                  <ArrowRight size={24} aria-hidden="true" />
                </button>
                <button className="secondary-button" type="button" onClick={startOnboarding}>
                  <RotateCcw size={22} aria-hidden="true" />
                  다시 보기
                </button>
              </div>
            </div>
          </section>
        ) : null}

        {screen === "tutorials" ? (
          <section className="tutorials" aria-labelledby="tutorials-title">
            <div className="section-heading">
              <p className="eyebrow">연습 고르기</p>
              <h1 id="tutorials-title">오늘 필요한 상황을 골라요</h1>
              <p className="lead">각 연습은 따라하기, 바꿔보기, 골라보기, 직접하기로 이어집니다.</p>
            </div>

            <div className="tutorial-grid">
              {tutorials.map((tutorial) => (
                <article className="tutorial-card" key={tutorial.id}>
                  <div>
                    <h2>{tutorial.title}</h2>
                    <p>{tutorial.situation}</p>
                  </div>
                  <button
                    className="secondary-button"
                    type="button"
                    onClick={() => openTutorial(tutorial)}
                  >
                    연습 시작
                    <ArrowRight size={22} aria-hidden="true" />
                  </button>
                </article>
              ))}
            </div>
          </section>
        ) : null}

        {screen === "tutorial" ? (
          <section className="tutorial" aria-labelledby="tutorial-title">
            <div className="tutorial-panel">
              <div className="step-status" aria-label={`튜토리얼 ${tutorialProgressText}`}>
                <span>{currentTutorialStep.label}</span>
                <strong>{tutorialProgressText}</strong>
              </div>

              <div className="section-heading">
                <p className="eyebrow">{selectedTutorial.title}</p>
                <h1 id="tutorial-title">{currentTutorialStep.title}</h1>
                <p className="lead">{currentTutorialStep.instruction}</p>
              </div>

              <PromptPractice
                draft={draft}
                selectedOption={selectedOption}
                step={currentTutorialStep}
                onDraftChange={setDraft}
                onOptionSelect={setSelectedOption}
              />

              <div className="step-actions">
                <button className="primary-button" type="button" onClick={goNextTutorial}>
                  {isLastTutorialStep ? "마치기" : "다음"}
                  {isLastTutorialStep ? (
                    <CheckCircle2 size={24} aria-hidden="true" />
                  ) : (
                    <ArrowRight size={24} aria-hidden="true" />
                  )}
                </button>
                <button className="quiet-button" type="button" onClick={goBackTutorial}>
                  <ArrowLeft size={22} aria-hidden="true" />
                  뒤로
                </button>
              </div>
            </div>
          </section>
        ) : null}
      </main>
    </div>
  );
}

type PromptPracticeProps = {
  draft: string;
  selectedOption: string;
  step: Tutorial["steps"][number];
  onDraftChange: (value: string) => void;
  onOptionSelect: (value: string) => void;
};

function PromptPractice({
  draft,
  selectedOption,
  step,
  onDraftChange,
  onOptionSelect,
}: PromptPracticeProps) {
  if (step.kind === "choose") {
    return (
      <div className="practice-box">
        <p className="sample-prompt">{step.prompt}</p>
        <div className="choice-list" aria-label="답변 방식 선택">
          {step.options?.map((option) => (
            <button
              className={selectedOption === option ? "choice-button selected" : "choice-button"}
              key={option}
              type="button"
              onClick={() => onOptionSelect(option)}
            >
              {option}
            </button>
          ))}
        </div>
        {selectedOption ? <p className="result-note">좋아요. "{selectedOption}" 방식으로 부탁해요.</p> : null}
      </div>
    );
  }

  if (step.kind === "follow") {
    return (
      <div className="practice-box">
        <p className="sample-prompt">{step.prompt}</p>
        <button className="secondary-button" type="button" onClick={() => onDraftChange(step.prompt)}>
          이 문장 따라하기
        </button>
      </div>
    );
  }

  return (
    <label className="practice-box">
      <span>{step.kind === "fill" ? "바꿔 볼 문장" : "내가 써 보는 문장"}</span>
      <textarea
        value={draft}
        rows={5}
        onChange={(event) => onDraftChange(event.target.value)}
      />
    </label>
  );
}
