export type TutorialStepKind = "follow" | "fill" | "choose" | "write";

export type TutorialStep = {
  kind: TutorialStepKind;
  label: string;
  title: string;
  instruction: string;
  prompt: string;
  options?: string[];
};

export type Tutorial = {
  id: string;
  title: string;
  situation: string;
  steps: TutorialStep[];
};

export const tutorials: Tutorial[] = [
  {
    id: "notice",
    title: "안내문 쉽게 보기",
    situation: "공공기관, 병원, 은행 안내문이 어렵게 느껴질 때",
    steps: [
      {
        kind: "follow",
        label: "따라하기",
        title: "예시 문장을 그대로 눌러봐요",
        instruction: "처음에는 바꾸지 않아도 됩니다. 문장이 길어 보여도 그대로 따라 해요.",
        prompt: "이 안내문을 쉬운 말로 바꿔줘. 중요한 내용은 세 가지만 알려줘.",
      },
      {
        kind: "fill",
        label: "바꿔보기",
        title: "상황 한 가지만 바꿔봐요",
        instruction: "괄호 안의 말만 지금 상황에 맞게 바꾸면 됩니다.",
        prompt: "이 (병원 안내문)을 쉬운 말로 바꿔줘. 내가 해야 할 일만 알려줘.",
      },
      {
        kind: "choose",
        label: "골라보기",
        title: "원하는 답변 모양을 골라요",
        instruction: "AI에게 어떤 식으로 알려 달라고 할지 하나만 고르면 됩니다.",
        prompt: "답변은 내가 고른 방식으로 정리해줘.",
        options: ["아주 짧게", "순서대로", "가족에게 물어볼 말로"],
      },
      {
        kind: "write",
        label: "직접하기",
        title: "내 말로 한 문장만 써봐요",
        instruction: "완벽하지 않아도 괜찮아요. AI가 알아들을 수 있게 조금만 적어도 됩니다.",
        prompt: "이 안내문에서 제가 꼭 확인해야 할 일을 쉽게 알려주세요.",
      },
    ],
  },
  {
    id: "message",
    title: "문자 답장 다듬기",
    situation: "답장을 어떻게 써야 할지 막막할 때",
    steps: [
      {
        kind: "follow",
        label: "따라하기",
        title: "답장 부탁 문장을 따라 해요",
        instruction: "상대에게 보낼 문장을 AI에게 먼저 만들어 달라고 해요.",
        prompt: "이 문자에 공손하게 답장하는 문장을 만들어줘. 너무 길지 않게 해줘.",
      },
      {
        kind: "fill",
        label: "바꿔보기",
        title: "말투만 바꿔봐요",
        instruction: "괄호 안에 원하는 말투를 넣어봅니다.",
        prompt: "이 문자에 (부드럽고 짧게) 답장하는 문장을 만들어줘.",
      },
      {
        kind: "choose",
        label: "골라보기",
        title: "답장 느낌을 골라요",
        instruction: "마음에 드는 느낌을 하나 고르면 AI가 그 방향으로 정리합니다.",
        prompt: "답장은 내가 고른 느낌으로 써줘.",
        options: ["친절하게", "짧고 분명하게", "거절하지만 부드럽게"],
      },
      {
        kind: "write",
        label: "직접하기",
        title: "상황을 한 줄로 적어요",
        instruction: "무슨 답장을 해야 하는지만 편하게 적어봅니다.",
        prompt: "친구가 내일 약속 시간을 바꾸자고 했어요. 예의 있게 답장하고 싶어요.",
      },
    ],
  },
  {
    id: "kiosk",
    title: "키오스크 주문 연습",
    situation: "매장에 가기 전에 주문 말을 미리 연습하고 싶을 때",
    steps: [
      {
        kind: "follow",
        label: "따라하기",
        title: "주문 연습 문장을 따라 해요",
        instruction: "AI에게 주문 순서를 미리 물어보는 연습입니다.",
        prompt: "카페 키오스크에서 따뜻한 아메리카노 한 잔을 주문하는 순서를 쉽게 알려줘.",
      },
      {
        kind: "fill",
        label: "바꿔보기",
        title: "메뉴 이름만 바꿔봐요",
        instruction: "괄호 안의 메뉴만 내가 먹고 싶은 것으로 바꾸면 됩니다.",
        prompt: "키오스크에서 (김치찌개)을 주문하는 순서를 천천히 알려줘.",
      },
      {
        kind: "choose",
        label: "골라보기",
        title: "도움받을 방식을 골라요",
        instruction: "매장에서 바로 떠올릴 수 있는 방식으로 골라봅니다.",
        prompt: "키오스크 앞에서 볼 수 있게 내가 고른 방식으로 알려줘.",
        options: ["버튼 누르는 순서", "직원에게 물어볼 말", "실수했을 때 다시 하는 법"],
      },
      {
        kind: "write",
        label: "직접하기",
        title: "가고 싶은 매장을 적어요",
        instruction: "정확한 매장 이름을 몰라도 괜찮아요. 음식 종류만 적어도 됩니다.",
        prompt: "분식집 키오스크에서 떡볶이와 김밥을 주문하는 순서를 알려주세요.",
      },
    ],
  },
];
