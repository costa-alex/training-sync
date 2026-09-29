import {Component, DestroyRef, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {formatDate} from "utils/date-formatter";
import {WorkoutClient} from "infrastructure/client/workout.client";
import {NotificationService} from "infrastructure/notification.service";
import {finalize} from "rxjs";
import {MatGridListModule} from "@angular/material/grid-list";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";

import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {Platform} from "infrastructure/platform";

@Component({
    selector: 'tp-copy-calendar-to-library',
    imports: [
    MatGridListModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatSelectModule,
    MatCheckboxModule
],
    templateUrl: './tp-copy-calendar-to-library.component.html',
    styleUrl: './tp-copy-calendar-to-library.component.scss'
})
export class TpCopyCalendarToLibraryComponent {
  readonly selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE', 'MTB', 'RUN'];
  readonly direction = Platform.DIRECTION_TP_INT
  readonly planType = [
    {name: 'Plan', value: true},
    {name: 'Folder', value: false}
  ]

  trainingTypes = [
    {title: "Ride", value: "BIKE"},
    {title: "MTB", value: "MTB"},
    {title: "Virtual Bike", value: "VIRTUAL_BIKE"},
    {title: "Run", value: "RUN"},
    {title: "Swim", value: "SWIM"},
    {title: "Walk", value: "WALK"},
    {title: "Strength Training", value: "STRENGTH"},
    {title: "Any other", value: "UNKNOWN"},
  ]

  formGroup: FormGroup = this.formBuilder.group({
    name: ['My New Library', Validators.required],
    trainingTypes: [this.selectedTrainingTypes, Validators.required],
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
    isPlan: [true, Validators.required],
  });
  inProgress = false

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private notificationService: NotificationService
  ) {
  }

  copyWorkoutsSubmit(): void {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    this.inProgress = true
    const name = this.formGroup.value.name
    const trainingTypes = this.formGroup.value.trainingTypes
    const startDate = formatDate(this.formGroup.value.startDate)
    const endDate = formatDate(this.formGroup.value.endDate)
    const isPlan = this.formGroup.value.isPlan
    this.workoutClient.copyCalendarToLibrary(name, startDate, endDate, trainingTypes, this.direction, isPlan).pipe(
      finalize(() => this.inProgress = false),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (response) => {
        this.notificationService.copyCalendarToLibraryCompleted(response, name)
      },
      error: () => {
        this.notificationService.error('Unable to copy workouts to library.')
      }
    })
  }
}
