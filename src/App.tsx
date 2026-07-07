import { ArrowLeft, ArrowRight, CheckCircle2, Home, ListChecks, RotateCcw } from "lucide-react";
import { useMemo, useState } from "react";
import { guides, type Guide } from "./tutorialData";
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
    title: "ChatGPT에 들어가요",
    body: "삼성 인터넷이나 Chrome에서 chatgpt.com을 열고, 로그인한 뒤 아래 입력칸에 말을 적어볼 거예요.",
    action: "다음으로",
  },
  {
    eyebrow: "3단계",
    title: "생각을 말로 풀어봐요",
    body: "처음에는 예시 문장을 그대로 써도 됩니다. ChatGPT가 질문을 하나씩 하도록 부탁하면 훨씬 편해요.",
    action: "첫 가이드 보기",
  },
];

type Screen = "home" | "onboarding" | "ready" | "guides" | "guide";

export function App() {
  const [screen, setScreen] = useState<Screen>("home");
  const [onboardingStepIndex, setOnboardingStepIndex] = useState(0);
  const [selectedGuide, setSelectedGuide] = useState<Guide>(guides[0]);
  const [guideStepIndex, setGuideStepIndex] = useState(0);

  const currentStep = onboardingSteps[onboardingStepIndex];
  const currentGuideStep = selectedGuide.steps[guideStepIndex];
  const isLastOnboardingStep = onboardingStepIndex === onboardingSteps.length - 1;
  const isLastGuideStep = guideStepIndex === selectedGuide.steps.length - 1;

  const onboardingProgressText = useMemo(
    () => `${onboardingStepIndex + 1} / ${onboardingSteps.length}`,
    [onboardingStepIndex],
  );

  const guideProgressText = useMemo(
    () => `${guideStepIndex + 1} / ${selectedGuide.steps.length}`,
    [guideStepIndex, selectedGuide.steps.length],
  );

  const startOnboarding = () => {
    setOnboardingStepIndex(0);
    setScreen("onboarding");
  };

  const openGuide = (guide: Guide) => {
    setSelectedGuide(guide);
    setGuideStepIndex(0);
    setScreen("guide");
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

  const goNextGuide = () => {
    if (isLastGuideStep) {
      setScreen("guides");
      return;
    }

    setGuideStepIndex((index) => index + 1);
  };

  const goBackGuide = () => {
    if (guideStepIndex === 0) {
      setScreen("guides");
      return;
    }

    setGuideStepIndex((index) => index - 1);
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
        <p>ChatGPT 생각정리 가이드</p>
      </header>

      <main>
        {screen === "home" ? (
          <section className="home" aria-labelledby="home-title">
            <div className="intro">
              <p className="eyebrow">삼성폰에서 천천히</p>
              <h1 id="home-title">ChatGPT에 말 걸어봐요</h1>
              <p className="lead">
                어디에 들어가서, 어디에 쓰면 되는지부터 짧게 안내합니다. 익숙해지면
                ChatGPT와 이야기하며 마음과 생각을 정리해볼 수 있어요.
              </p>
              <div className="actions">
                <button className="primary-button" type="button" onClick={startOnboarding}>
                  시작하기
                  <ArrowRight size={24} aria-hidden="true" />
                </button>
                <button className="secondary-button" type="button" onClick={() => setScreen("guides")}>
                  가이드 보기
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
                <button className="quiet-button" type="button" onClick={() => setScreen("guides")}>
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
              <h1 id="ready-title">먼저 접속부터 해볼게요</h1>
              <p className="lead">
                ChatGPT 페이지에 들어가고 로그인한 다음, 아래 입력칸에 예시 문장을 써보는
                흐름으로 안내합니다.
              </p>
              <div className="actions">
                <button className="primary-button" type="button" onClick={() => openGuide(guides[0])}>
                  첫 가이드로 가기
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

        {screen === "guides" ? (
          <section className="tutorials" aria-labelledby="guides-title">
            <div className="section-heading">
              <p className="eyebrow">가이드 고르기</p>
              <h1 id="guides-title">필요한 것만 짧게 봐요</h1>
              <p className="lead">
                ChatGPT 접속, 첫 문장 입력, 생각정리 대화만 간단하게 안내합니다.
              </p>
            </div>

            <div className="tutorial-grid">
              {guides.map((guide) => (
                <article className="tutorial-card" key={guide.id}>
                  <div>
                    <h2>{guide.title}</h2>
                    <p>{guide.summary}</p>
                  </div>
                  <button className="secondary-button" type="button" onClick={() => openGuide(guide)}>
                    보기
                    <ArrowRight size={22} aria-hidden="true" />
                  </button>
                </article>
              ))}
            </div>
          </section>
        ) : null}

        {screen === "guide" ? (
          <section className="tutorial" aria-labelledby="guide-title">
            <div className="tutorial-panel">
              <div className="step-status" aria-label={`가이드 ${guideProgressText}`}>
                <span>{currentGuideStep.label}</span>
                <strong>{guideProgressText}</strong>
              </div>

              <div className="section-heading">
                <p className="eyebrow">{selectedGuide.title}</p>
                <h1 id="guide-title">{currentGuideStep.title}</h1>
                <p className="lead">{currentGuideStep.body}</p>
              </div>

              {currentGuideStep.prompt ? (
                <div className="practice-box">
                  <span>입력칸에 써볼 말</span>
                  <p className="sample-prompt">{currentGuideStep.prompt}</p>
                </div>
              ) : null}

              <div className="step-actions">
                <button className="primary-button" type="button" onClick={goNextGuide}>
                  {isLastGuideStep ? "가이드 끝" : "다음"}
                  {isLastGuideStep ? (
                    <CheckCircle2 size={24} aria-hidden="true" />
                  ) : (
                    <ArrowRight size={24} aria-hidden="true" />
                  )}
                </button>
                <button className="quiet-button" type="button" onClick={goBackGuide}>
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
