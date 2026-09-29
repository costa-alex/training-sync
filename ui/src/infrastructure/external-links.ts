export const GITHUB_REPO_URL = 'https://github.com/costa-alex/workout-relay';

export const GITHUB_REPO_OWNER = 'costa-alex';
export const GITHUB_REPO_NAME = 'workout-relay';

export function githubReadmeSectionUrl(section: string): string {
  return `${GITHUB_REPO_URL}?tab=readme-ov-file#${section}`;
}
