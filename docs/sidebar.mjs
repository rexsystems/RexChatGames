const root = "rexchatgames";

export const rexchatgames = {
  label: "RexChatGames",
  items: [
    { label: "Overview", slug: root },
    {
      label: "Getting started",
      items: [
        { label: "Installation", slug: `${root}/getting-started/installation` },
        { label: "Requirements", slug: `${root}/getting-started/requirements` },
      ],
    },
    {
      label: "Configuration",
      items: [
        { label: "Settings", slug: `${root}/configuration/overview` },
        { label: "Games", slug: `${root}/configuration/games` },
        { label: "Word lists", slug: `${root}/configuration/word-lists` },
      ],
    },
    {
      label: "Reference",
      items: [
        { label: "Commands", slug: `${root}/reference/commands` },
        { label: "Permissions", slug: `${root}/reference/permissions` },
        { label: "Placeholders", slug: `${root}/reference/placeholders` },
      ],
    },
    { label: "FAQ", slug: `${root}/faq` },
  ],
};
