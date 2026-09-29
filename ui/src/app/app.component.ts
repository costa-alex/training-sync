import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { catchError, of } from 'rxjs';
import { gt } from 'semver';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';

import { TopBarComponent } from 'app/top-bar/top-bar.component';
import {
  ApplicationInfoClient
} from 'infrastructure/client/application-info.client';
import { GitHubClient } from 'infrastructure/client/github.client';
import { GITHUB_REPO_URL } from 'infrastructure/external-links';
import { ThemeService } from 'infrastructure/theme.service';

@Component({
    selector: 'app-root',
    imports: [
        RouterOutlet,
        RouterLink,
        TopBarComponent,
        MatSidenavModule,
        MatListModule,
        MatDividerModule,
        MatIconModule,
        MatBadgeModule,
        MatTooltipModule
    ],
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {

  appVersion = '';
  updateAvailableBadgeHidden = true;
  githubLink = GITHUB_REPO_URL;

  private readonly destroyRef = inject(DestroyRef);

  menuButtons = [
    { icon: 'home', name: 'Home', url: '/home' },
    {
      icon: 'directions_bike',
      name: 'TrainerRoad',
      url: '/trainer-road'
    },
    {
      icon: 'timeline',
      name: 'TrainingPeaks',
      url: '/training-peaks'
    },
    {
      icon: 'schedule',
      name: 'Sync Center',
      url: '/sync-center'
    },
    {
      icon: 'settings',
      name: 'Settings',
      url: '/settings'
    }
  ];

  constructor(
    protected router: Router,
    private githubClient: GitHubClient,
    private applicationInfoClient: ApplicationInfoClient,
    private themeService: ThemeService
  ) {
  }

  ngOnInit(): void {
    this.applicationInfoClient.getVersion()
      .pipe(
        catchError(() => of('')),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(appVersion => {
        this.appVersion = appVersion;
      });

    this.githubClient.getLatestRelease()
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(latestRelease => {
        if (latestRelease && this.appVersion && gt(latestRelease.version, this.appVersion)) {
          this.updateAvailableBadgeHidden = false;
          this.githubLink = latestRelease.url;
        }
      });
  }
}
