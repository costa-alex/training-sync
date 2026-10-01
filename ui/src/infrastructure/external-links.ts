export const GITHUB_REPO_URL = 'https://github.com/costa-alex/training-sync';

export const GITHUB_REPO_OWNER = 'costa-alex';
export const GITHUB_REPO_NAME = 'training-sync';

export function githubReadmeSectionUrl(section: string): string {
  return `${GITHUB_REPO_URL}?tab=readme-ov-file#${section}`;
}
