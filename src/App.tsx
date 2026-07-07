import { ArrowRight, Home } from "lucide-react";
import "./App.css";

export function App() {
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

      <main className="home">
        <section className="intro" aria-labelledby="home-title">
          <p className="eyebrow">삼성폰에서 바로 쓰는 연습</p>
          <h1 id="home-title">천천히 물어봐도 괜찮아요</h1>
          <p className="lead">
            AI에게 질문하는 일을 작은 단계로 나누어 연습합니다. 잘못 눌러도 다시 하면 되고,
            어려운 말은 쉬운 말로 바꿔 볼 수 있어요.
          </p>
          <div className="actions">
            <button className="primary-button" type="button">
              시작하기
              <ArrowRight size={24} aria-hidden="true" />
            </button>
            <button className="secondary-button" type="button">
              홈 화면에 추가하기
            </button>
          </div>
        </section>
      </main>
    </div>
  );
}
