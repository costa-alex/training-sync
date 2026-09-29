import {Injectable} from '@angular/core';
import {map, Observable} from "rxjs";
import { HttpClient } from "@angular/common/http";
import { GITHUB_REPO_NAME, GITHUB_REPO_OWNER } from 'infrastructure/external-links';

interface GitHubReleaseResponse {
  tag_name: string;
  html_url: string;
}


@Injectable({
  providedIn: 'root'
})
export class GitHubClient {
  private static apiUrl = 'https://api.github.com';

  constructor(
    private httpClient: HttpClient
  ) {
  }

  getLatestRelease(): Observable<Release> {
    return this.httpClient
      .get<GitHubReleaseResponse>(
        `${GitHubClient.apiUrl}/repos/${GITHUB_REPO_OWNER}/${GITHUB_REPO_NAME}/releases/latest`
      )
      .pipe(
        map(response => new Release(response)),
      )
  }
}

export class Release {
  version: string;
  url: string;

  constructor(json: GitHubReleaseResponse) {
    this.version = json.tag_name.replace(/^v/, '');
    this.url = json.html_url;
  }
}
