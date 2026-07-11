import {
  ArrowLeft,
  ArrowRight,
  CheckCircle2,
  ClipboardCheck,
  ExternalLink,
  Globe2,
  History,
  Home,
  LogIn,
  MessageCircle,
  PlusCircle,
} from "lucide-react";
import { type ComponentType, useEffect, useMemo, useState } from "react";
import "./App.css";

type ManualStep = {
  id: string;
  label: string;
  title: string;
  summary: string;
  instruction: string;
  checklist: string[];
  nextAction: string;
  icon: ComponentType<{ size?: number; strokeWidth?: number; "aria-hidden"?: boolean }>;
};

const CHATGPT_URL = "https://chatgpt.com/";

const manualSteps: ManualStep[] = [
  {
    id: "connect",
    label: "01",
    title: "접속하기",
    summary: "ChatGPT 화면이 열리는지 먼저 확인합니다.",
    instruction:
      "오른쪽 작업 창에 ChatGPT 화면이 보이면 그대로 진행하세요. 보이지 않으면 큰 버튼을 눌러 새 탭에서 열면 됩니다.",
    checklist: ["ChatGPT 화면이 보이는지 확인하기", "안 보이면 새 탭으로 열기", "주소가 chatgpt.com인지 확인하기"],
    nextAction: "화면이 열렸는지 확인했어요",
    icon: Globe2,
  },
  {
    id: "guest-chat",
    label: "02",
    title: "비로그인 상태로 대화하기",
    summary: "로그인 전에도 입력칸이 보이면 짧게 말을 걸어봅니다.",
    instruction:
      "로그인 없이 입력칸이 보이면 아래쪽 입력칸에 짧은 문장을 써보세요. 보이지 않거나 제한이 있으면 다음 단계에서 로그인합니다.",
    checklist: ["아래쪽 입력칸 찾기", "짧은 문장 하나 입력하기", "응답이 오면 천천히 읽기"],
    nextAction: "대화를 시도했어요",
    icon: MessageCircle,
  },
  {
    id: "login",
    label: "03",
    title: "로그인하기",
    summary: "기존 계정으로 들어가 이전 대화를 사용할 준비를 합니다.",
    instruction:
      "로그인 버튼을 눌러 본인이 쓰던 방법으로 접속하세요. 이 앱은 비밀번호나 인증번호를 받거나 저장하지 않습니다.",
    checklist: ["Log in 또는 로그인 버튼 누르기", "원래 쓰던 로그인 방법 선택하기", "비밀번호는 ChatGPT 화면에만 입력하기"],
    nextAction: "로그인을 마쳤어요",
    icon: LogIn,
  },
  {
    id: "continue",
    label: "04",
    title: "이전 대화 이어가기",
    summary: "전에 나눴던 대화가 있다면 목록에서 다시 엽니다.",
    instruction:
      "왼쪽 또는 위쪽에 대화 목록이 보이면 이어가고 싶은 제목을 누르세요. 제목이 잘 안 보이면 화면을 천천히 확대하거나 새 탭을 사용하세요.",
    checklist: ["대화 목록 찾기", "이어갈 대화 제목 누르기", "마지막 답변 아래 입력칸 확인하기"],
    nextAction: "이전 대화를 열었어요",
    icon: History,
  },
  {
    id: "new-chat",
    label: "05",
    title: "새 대화 시작하기",
    summary: "새 주제로 이야기하고 싶을 때 새 대화를 엽니다.",
    instruction:
      "새 대화 버튼을 누른 뒤, 입력칸에 하고 싶은 말을 한 문장으로 적어보세요. 처음 문장은 짧아도 괜찮습니다.",
    checklist: ["새 대화 버튼 찾기", "입력칸에 한 문장 적기", "보내기 버튼 누르기"],
    nextAction: "새 대화를 시작했어요",
    icon: PlusCircle,
  },
  {
    id: "organize",
    label: "06",
    title: "대화 내용 정리/복사하기",
    summary: "대화가 끝나면 중요한 내용을 쉬운 말로 정리합니다.",
    instruction:
      "ChatGPT에게 지금까지의 내용을 보기 쉽게 정리해 달라고 말하세요. 필요한 문장은 길게 누르거나 복사 버튼으로 옮길 수 있습니다.",
    checklist: ["정리해 달라고 요청하기", "중요한 문장 확인하기", "필요한 내용 복사하기"],
    nextAction: "정리까지 끝냈어요",
    icon: ClipboardCheck,
  },
];

export function App() {
  const [activeStepId, setActiveStepId] = useState(manualSteps[0].id);
  const [completedSteps, setCompletedSteps] = useState<Set<string>>(new Set());
  const [frameStatus, setFrameStatus] = useState<"loading" | "ready" | "blocked">("loading");

  const activeStepIndex = manualSteps.findIndex((step) => step.id === activeStepId);
  const activeStep = manualSteps[activeStepIndex] ?? manualSteps[0];
  const StepIcon = activeStep.icon;
  const completedCount = completedSteps.size;
  const progressPercent = Math.round((completedCount / manualSteps.length) * 100);
  const isFirstStep = activeStepIndex === 0;
  const isLastStep = activeStepIndex === manualSteps.length - 1;

  const statusText = useMemo(() => {
    if (frameStatus === "ready") {
      return "작업 창이 열렸어요";
    }

    if (frameStatus === "blocked") {
      return "작업 창이 막히면 새 탭을 쓰세요";
    }

    return "작업 창을 여는 중이에요";
  }, [frameStatus]);

  useEffect(() => {
    setFrameStatus("loading");

    const blockedTimer = window.setTimeout(() => {
      setFrameStatus((status) => (status === "loading" ? "blocked" : status));
    }, 2800);

    return () => window.clearTimeout(blockedTimer);
  }, [activeStepId]);

  const moveStep = (direction: "previous" | "next") => {
    const nextIndex = direction === "next" ? activeStepIndex + 1 : activeStepIndex - 1;
    const nextStep = manualSteps[Math.min(Math.max(nextIndex, 0), manualSteps.length - 1)];
    setActiveStepId(nextStep.id);
  };

  const completeActiveStep = () => {
    setCompletedSteps((current) => {
      const next = new Set(current);
      next.add(activeStep.id);
      return next;
    });

    if (!isLastStep) {
      moveStep("next");
    }
  };

  return (
    <div className="app-shell">
      <header className="app-header" aria-label="앱 정보">
        <button className="brand-button" type="button" onClick={() => setActiveStepId(manualSteps[0].id)}>
          <span className="brand-mark" aria-hidden="true">
            <Home size={28} strokeWidth={2.5} />
          </span>
          <span>명순</span>
        </button>
        <div className="header-copy">
          <strong>큰 글씨 실행 메뉴얼</strong>
          <span>Fold7에서 천천히 따라 하는 ChatGPT 안내</span>
        </div>
      </header>

      <main className="manual-layout" aria-labelledby="app-title">
        <aside className="manual-index" aria-label="실행 메뉴얼 목차">
          <div className="index-heading">
            <p className="eyebrow">첫 메뉴</p>
            <h1 id="app-title">GPT와 대화하기</h1>
            <p>한 단계씩 천천히 진행하세요.</p>
          </div>

          <div className="progress-box" aria-label={`전체 진행률 ${progressPercent}%`}>
            <div>
              <span>진행률</span>
              <strong>
                {completedCount} / {manualSteps.length}
              </strong>
            </div>
            <div className="progress-track" aria-hidden="true">
              <span style={{ width: `${progressPercent}%` }} />
            </div>
          </div>

          <nav className="step-list" aria-label="GPT와 대화하기 단계">
            {manualSteps.map((step) => {
              const Icon = step.icon;
              const isActive = step.id === activeStep.id;
              const isComplete = completedSteps.has(step.id);

              return (
                <button
                  className={`step-button${isActive ? " is-active" : ""}`}
                  type="button"
                  key={step.id}
                  onClick={() => setActiveStepId(step.id)}
                  aria-current={isActive ? "step" : undefined}
                >
                  <span className="step-number">{step.label}</span>
                  <span className="step-icon" aria-hidden="true">
                    {isComplete ? <CheckCircle2 size={30} strokeWidth={2.4} /> : <Icon size={30} strokeWidth={2.4} />}
                  </span>
                  <span className="step-copy">
                    <strong>{step.title}</strong>
                    <small>{step.summary}</small>
                  </span>
                </button>
              );
            })}
          </nav>
        </aside>

        <section className="guide-stage" aria-labelledby="step-title">
          <div className="guide-panel">
            <div className="step-kicker">
              <span>{activeStep.label} 단계</span>
              <strong>{statusText}</strong>
            </div>

            <div className="guide-title-row">
              <span className="large-step-icon" aria-hidden="true">
                <StepIcon size={42} strokeWidth={2.4} />
              </span>
              <div>
                <h2 id="step-title">{activeStep.title}</h2>
                <p>{activeStep.instruction}</p>
              </div>
            </div>

            <div className="work-area">
              <div className="frame-shell" aria-label="ChatGPT 작업 창">
                <div className="frame-toolbar">
                  <strong>ChatGPT 화면</strong>
                  <a className="open-link" href={CHATGPT_URL} target="_blank" rel="noreferrer">
                    새 탭에서 열기
                    <ExternalLink size={24} aria-hidden="true" />
                  </a>
                </div>

                <div className="frame-wrap">
                  <iframe
                    key={activeStep.id}
                    src={CHATGPT_URL}
                    title={`ChatGPT ${activeStep.title} 단계 작업 창`}
                    loading="lazy"
                    onLoad={() => setFrameStatus("ready")}
                    sandbox="allow-forms allow-popups allow-same-origin allow-scripts"
                  />

                  {frameStatus !== "ready" ? (
                    <div className="frame-fallback" role="status">
                      <strong>
                        {frameStatus === "loading" ? "작업 창을 여는 중이에요." : "화면이 안 보이면 괜찮아요."}
                      </strong>
                      <p>
                        {frameStatus === "loading"
                          ? "잠시 기다린 뒤에도 화면이 비어 있으면 새 탭에서 크게 열어 진행하세요."
                          : "ChatGPT는 보안 설정 때문에 앱 안의 작은 창에서 막힐 수 있습니다. 새 탭에서 열고, 이 화면은 설명서로 같이 보세요."}
                      </p>
                      <a className="primary-button" href={CHATGPT_URL} target="_blank" rel="noreferrer">
                        새 탭에서 크게 열기
                        <ExternalLink size={26} aria-hidden="true" />
                      </a>
                    </div>
                  ) : null}
                </div>
              </div>

              <div className="assist-panel" aria-label="현재 단계 안내">
                <h3>지금 할 일</h3>
                <ul className="check-list">
                  {activeStep.checklist.map((item) => (
                    <li key={item}>
                      <CheckCircle2 size={28} aria-hidden="true" />
                      <span>{item}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            <div className="step-actions" aria-label="단계 이동">
              <button className="secondary-button" type="button" onClick={() => moveStep("previous")} disabled={isFirstStep}>
                <ArrowLeft size={26} aria-hidden="true" />
                이전 단계
              </button>
              <button className="primary-button" type="button" onClick={completeActiveStep}>
                {activeStep.nextAction}
                {isLastStep ? <CheckCircle2 size={28} aria-hidden="true" /> : <ArrowRight size={28} aria-hidden="true" />}
              </button>
              <button className="secondary-button" type="button" onClick={() => moveStep("next")} disabled={isLastStep}>
                다음 단계
                <ArrowRight size={26} aria-hidden="true" />
              </button>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
