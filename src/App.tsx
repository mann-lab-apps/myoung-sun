import { ArrowLeft, ArrowRight, CheckCircle2, Home, RotateCcw } from "lucide-react";
import { useMemo, useState } from "react";
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

type Screen = "home" | "onboarding" | "ready" | "firstPractice";

export function App() {
  const [screen, setScreen] = useState<Screen>("home");
  const [stepIndex, setStepIndex] = useState(0);
  const currentStep = onboardingSteps[stepIndex];
  const isLastStep = stepIndex === onboardingSteps.length - 1;

  const progressText = useMemo(
    () => `${stepIndex + 1} / ${onboardingSteps.length}`,
    [stepIndex],
  );

  const startOnboarding = () => {
    setStepIndex(0);
    setScreen("onboarding");
  };

  const goNext = () => {
    if (isLastStep) {
      setScreen("ready");
      return;
    }

    setStepIndex((index) => index + 1);
  };

  const goBack = () => {
    if (stepIndex === 0) {
      setScreen("home");
      return;
    }

    setStepIndex((index) => index - 1);
  };

  return (
    <div className="app-shell">
      <header className="app-header" aria-label="앱 정보">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true">
            <Home size={24} strokeWidth={2.4} />
          </span>
          <span>명선</span>
        </div>
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
                <button className="secondary-button" type="button" onClick={() => setScreen("ready")}>
                  바로 연습하기
                </button>
              </div>
            </div>
          </section>
        ) : null}

        {screen === "onboarding" ? (
          <section className="onboarding" aria-labelledby="onboarding-title">
            <div className="step-panel">
              <div className="step-status" aria-label={`온보딩 ${progressText}`}>
                <span>{currentStep.eyebrow}</span>
                <strong>{progressText}</strong>
              </div>

              <h1 id="onboarding-title">{currentStep.title}</h1>
              <p className="lead">{currentStep.body}</p>

              <div className="step-actions">
                <button className="primary-button" type="button" onClick={goNext}>
                  {currentStep.action}
                  {isLastStep ? (
                    <CheckCircle2 size={24} aria-hidden="true" />
                  ) : (
                    <ArrowRight size={24} aria-hidden="true" />
                  )}
                </button>
                <button className="quiet-button" type="button" onClick={goBack}>
                  <ArrowLeft size={22} aria-hidden="true" />
                  뒤로
                </button>
                <button className="quiet-button" type="button" onClick={() => setScreen("ready")}>
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
                다음 단계에서는 예시 문장을 그대로 눌러 보고, 빈칸만 바꿔 보면서 AI 질문을
                연습합니다.
              </p>
              <div className="actions">
                <button className="primary-button" type="button" onClick={() => setScreen("firstPractice")}>
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

        {screen === "firstPractice" ? (
          <section className="practice-start" aria-labelledby="practice-start-title">
            <div className="ready-panel">
              <p className="eyebrow">첫 연습</p>
              <h1 id="practice-start-title">이 문장을 그대로 따라 해봐요</h1>
              <p className="sample-prompt">
                안내문을 쉬운 말로 바꿔줘. 중요한 내용은 세 가지만 알려줘.
              </p>
              <div className="actions">
                <button className="primary-button" type="button">
                  따라 해봤어요
                  <CheckCircle2 size={24} aria-hidden="true" />
                </button>
                <button className="secondary-button" type="button" onClick={startOnboarding}>
                  <RotateCcw size={22} aria-hidden="true" />
                  다시 보기
                </button>
              </div>
            </div>
          </section>
        ) : null}
      </main>
    </div>
  );
}
