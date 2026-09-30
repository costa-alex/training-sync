import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';

import { Platform } from 'infrastructure/platform';
import { ConfigurationClient, PlatformConnectionInfo } from 'infrastructure/client/configuration.client';

import {
  TrCopyCalendarToCalendarComponent
} from 'app/trainer-road/tr-copy-calendar-to-calendar/tr-copy-calendar-to-calendar.component';
import {
  TrCopyCalendarToLibraryComponent
} from 'app/trainer-road/tr-copy-calendar-to-library/tr-copy-calendar-to-library.component';
import {
  TrCopyLibraryToLibraryComponent
} from 'app/trainer-road/tr-copy-library-to-library/tr-copy-library-to-library.component';

@Component({
    selector: 'app-trainer-road',
    imports: [
    MatCardModule,
    MatProgressBarModule,
    MatIconModule,
    TrCopyLibraryToLibraryComponent,
    TrCopyCalendarToLibraryComponent,
    TrCopyCalendarToCalendarComponent
],
    templateUrl: './trainer-road.component.html',
    styleUrl: './trainer-road.component.scss'
})
export class TrainerRoadComponent implements OnInit {
  platformInfo: PlatformConnectionInfo | undefined;

  private readonly platform = Platform.TRAINER_ROAD;
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private configurationClient: ConfigurationClient
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.platformInfo(this.platform.key)
      .pipe(
        catchError(() => of({ isValid: false })),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(value => {
        this.platformInfo = value;
      });
  }
}