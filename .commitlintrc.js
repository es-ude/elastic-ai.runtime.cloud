module.exports = {
  extends: ["@commitlint/config-conventional"],
  rules: {
    "type-empty": [2, "never"],
    "type-case": [2, "always", "lower-case"],
    "type-enum": [
      2,
      "always",
      ["build", "ci", "feat", "fix", "docs", "style", "refactor", "revert", "chore", "wip", "perf"],
    ],
    "scope-empty": [2, "never"],
    "scope-case": [2, "always", "kebab-case"],
    "scope-enum": [
      2,
      "always",
      ["all", "application", "runtime", "workflow", "readme", "kubernetes", "docker", "gradle"],
    ],
  },
};
