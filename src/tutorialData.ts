export type Guide = {
  id: string;
  title: string;
  summary: string;
  steps: {
    label: string;
    title: string;
    body: string;
    prompt?: string;
  }[];
};

export const guides: Guide[] = [
  {
    id: "start-chatgpt",
    title: "ChatGPT 들어가기",
    summary: "어느 페이지에 들어가서 어디를 누르는지 먼저 확인해요.",
    steps: [
      {
        label: "1",
        title: "인터넷 앱을 열어요",
        body: "삼성 인터넷이나 Chrome을 열고 주소창에 chatgpt.com 을 입력해요.",
      },
      {
        label: "2",
        title: "로그인을 눌러요",
        body: "화면에 로그인이나 Log in 버튼이 보이면 눌러요. 예전에 쓴 방법이 있으면 같은 방법으로 들어가요.",
      },
      {
        label: "3",
        title: "아래 입력칸을 찾아요",
        body: "화면 아래쪽에 글을 쓰는 칸이 있어요. 거기에 하고 싶은 말을 적고 보내기 버튼을 눌러요.",
      },
    ],
  },
  {
    id: "clear-thoughts",
    title: "생각정리 시작하기",
    summary: "머릿속이 복잡할 때 첫 문장을 그대로 써봐요.",
    steps: [
      {
        label: "1",
        title: "첫 문장을 복사하듯 따라 써요",
        body: "처음에는 잘 쓰려고 하지 않아도 됩니다. 아래 문장을 그대로 입력해도 좋아요.",
        prompt:
          "내 생각을 정리하고 싶어. 지금 마음이 복잡한데, 질문을 하나씩 해주면서 내가 무엇을 걱정하는지 정리해줘.",
      },
      {
        label: "2",
        title: "답을 짧게 말해요",
        body: "ChatGPT가 물어보면 한 문장만 답해도 됩니다. 길게 설명하지 않아도 괜찮아요.",
        prompt: "내가 짧게 대답해도 이해해줘. 한 번에 질문 하나만 해줘.",
      },
      {
        label: "3",
        title: "마지막에 정리해 달라고 해요",
        body: "대화를 조금 한 뒤에는 내가 한 말을 보기 쉽게 묶어 달라고 부탁해요.",
        prompt: "지금까지 내가 말한 내용을 쉬운 말로 정리해줘. 내가 할 수 있는 작은 행동도 하나만 알려줘.",
      },
    ],
  },
  {
    id: "gentle-conversation",
    title: "상담하듯 대화하기",
    summary: "속상하거나 결정이 어려울 때 부드럽게 물어보는 방법이에요.",
    steps: [
      {
        label: "1",
        title: "마음을 먼저 말해요",
        body: "정답을 찾기보다 지금 기분을 말하는 것부터 시작해요.",
        prompt: "오늘 마음이 조금 무거워. 내 이야기를 차분히 들어주고, 바로 해결책부터 말하지 말아줘.",
      },
      {
        label: "2",
        title: "선택을 도와달라고 해요",
        body: "결정이 어려울 때는 장단점을 나눠 달라고 하면 생각이 정리됩니다.",
        prompt: "내가 고민하는 선택지를 장점과 걱정되는 점으로 나눠서 정리해줘.",
      },
      {
        label: "3",
        title: "중요한 일은 확인해요",
        body: "건강, 돈, 법처럼 중요한 일은 ChatGPT 답만 믿지 말고 가족이나 전문가에게 한 번 더 물어봐요.",
      },
    ],
  },
];
