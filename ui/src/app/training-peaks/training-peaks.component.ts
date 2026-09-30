import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {catchError, of} from 'rxjs';
import {
  TpCopyCalendarToCalendarComponent
} from "app/training-peaks/tp-copy-calendar-to-calendar/tp-copy-calendar-to-calendar.component";
import {
  TpCopyLibraryContainerComponent
} from "app/training-peaks/tp-copy-library-container/tp-copy-library-container.component";
import {
  TpCopyCalendarToLibraryComponent
} from "app/training-peaks/tp-copy-calendar-to-library/tp-copy-calendar-to-library.component";
import {MatCardModule} from "@angular/material/card";

import {ConfigurationClient, PlatformConnectionInfo} from "infrastructure/client/configuration.client";
import {Platform} from "infrastructure/platform";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatIconModule} from '@angular/material/icon';

@Component({
    selector: 'app-training-peaks',
    imports: [
    TpCopyCalendarToCalendarComponent,
    TpCopyLibraryContainerComponent,
    TpCopyCalendarToLibraryComponent,
    MatCardModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatIconModule
],
    templateUrl: './training-peaks.component.html',
    styleUrl: './training-peaks.component.scss'
})
export class TrainingPeaksComponent implements OnInit {
  platformInfo: PlatformConnectionInfo | undefined;

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private configurationClient: ConfigurationClient
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.platformInfo(Platform.TRAINING_PEAKS.key)
      .pipe(
        catchError(() => of({ isValid: false })),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(value => {
        this.platformInfo = value
      })
  }
}
