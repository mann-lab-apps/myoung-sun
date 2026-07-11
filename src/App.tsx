import {
  CheckCircle2,
  ExternalLink,
  Home,
  MessageCircle,
  PanelBottomOpen,
  ShieldCheck,
  Smartphone,
} from "lucide-react";
import "./App.css";

const CHATGPT_URL = "https://chatgpt.com/";

const guideSteps = [
  "ChatGPT 접속하기",
  "로그인 상태 확인하기",
  "새 대화 시작하기",
  "이전 대화 이어가기",
  "대화 내용 정리하기",
];

export function App() {
  return (
    <div className="app-shell">
      <header className="app-header" aria-label="앱 정보">
        <div className="brand-lockup">
          <span className="brand-mark" aria-hidden="true">
            <Home size={30} strokeWidth={2.5} />
          </span>
          <div>
            <strong>명순</strong>
            <span>큰 글씨 실행 메뉴얼</span>
          </div>
        </div>
      </header>

      <main className="webview-brief" aria-labelledby="home-title">
        <section className="hero-panel">
          <p className="eyebrow">Android WebView 전환</p>
          <h1 id="home-title">GPT와 대화하기</h1>
          <p className="lead">
            이 앱은 ChatGPT를 복제하지 않습니다. Android 앱에서 ChatGPT 웹 화면을 크게 열고,
            옆에서 단계별로 따라 할 수 있게 돕습니다.
          </p>

          <div className="action-row">
            <a className="primary-action" href={CHATGPT_URL} target="_blank" rel="noreferrer">
              ChatGPT 새 탭에서 열기
              <ExternalLink size={28} aria-hidden="true" />
            </a>
          </div>
        </section>

        <section className="direction-grid" aria-label="제품 방향">
          <article className="direction-card">
            <span aria-hidden="true">
              <Smartphone size={34} strokeWidth={2.4} />
            </span>
            <h2>WebView 최상위 화면</h2>
            <p>Android 앱에서는 `https://chatgpt.com`을 iframe이 아니라 WebView의 최상위 페이지로 엽니다.</p>
          </article>

          <article className="direction-card">
            <span aria-hidden="true">
              <PanelBottomOpen size={34} strokeWidth={2.4} />
            </span>
            <h2>큰 글씨 가이드</h2>
            <p>Fold7 커버 화면에서는 하단 패널, 펼친 화면에서는 옆 패널로 단계 안내를 제공합니다.</p>
          </article>

          <article className="direction-card">
            <span aria-hidden="true">
              <ShieldCheck size={34} strokeWidth={2.4} />
            </span>
            <h2>토큰 저장 없음</h2>
            <p>OpenAI API key, ChatGPT 비밀번호, 인증번호, 세션 값은 앱이 요구하거나 저장하지 않습니다.</p>
          </article>
        </section>

        <section className="steps-panel" aria-label="가이드 단계">
          <div>
            <MessageCircle size={36} strokeWidth={2.5} aria-hidden="true" />
            <h2>앱에서 보여줄 단계</h2>
          </div>
          <ol>
            {guideSteps.map((step) => (
              <li key={step}>
                <CheckCircle2 size={28} aria-hidden="true" />
                <span>{step}</span>
              </li>
            ))}
          </ol>
        </section>
      </main>
    </div>
  );
}
