import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatGridListModule} from "@angular/material/grid-list";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import { AsyncPipe } from "@angular/common";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {DateAdapter, MAT_DATE_LOCALE, MatNativeDateModule} from "@angular/material/core";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {ConfigurationClient} from "infrastructure/client/configuration.client";
import {NotificationService} from "infrastructure/notification.service";
import {filter, finalize, forkJoin, map} from "rxjs";
import {LibraryClient} from "infrastructure/client/library-client.service";
import {Platform} from "infrastructure/platform";
import {MatDialog} from "@angular/material/dialog";
import {
  TpCopyPlanWarningDialogComponent
} from "app/training-peaks/tp-copy-library-container/tp-copy-plan-warning-dialog/tp-copy-plan-warning-dialog.component";
import {formatDate} from "utils/date-formatter";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatIconModule} from "@angular/material/icon";
import {StepModifier} from "app/training-peaks/tp-copy-library-container/step-modifier";
import {LibraryContainer} from 'infrastructure/api-models';
import {MondayFirstDateAdapter} from 'infrastructure/monday-first-date-adapter';

@Component({
    selector: 'tp-copy-library-container',
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
    MatCheckboxModule,
    AsyncPipe,
    MatTooltipModule,
    MatIconModule
],
    providers: [
    {provide: MAT_DATE_LOCALE, useValue: 'en-GB'},
    {provide: DateAdapter, useClass: MondayFirstDateAdapter}
],
    templateUrl: './tp-copy-library-container.component.html',
    styleUrl: './tp-copy-library-container.component.scss'
})
export class TpCopyLibraryContainerComponent implements OnInit {
  private readonly mondayDate = formatDate(this.getMonday(new Date()))

  formGroup: FormGroup = this.formBuilder.group({
    plan: [null, Validators.required],
    newName: [null, Validators.required],
    newStartDate: [this.mondayDate, Validators.required],
    stepModifier: ['NONE', Validators.required],
  });

  isPlanSelected = this.formGroup.controls['plan'].valueChanges.pipe(
    map(value => !!value?.isPlan)
  )
  submitInProgress = false
  loadingInProgress = false

  stepModifiers = StepModifier.stepModifiers;
  plans: { name: string; value: LibraryContainer }[];
  config: Record<string, string | boolean | null> = {};

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private planClient: LibraryClient,
    private configurationClient: ConfigurationClient,
    private notificationService: NotificationService,
  ) {
  }

  ngOnInit(): void {
    this.formGroup.disable()
    this.loadingInProgress = true

    forkJoin({
      config: this.getConfig(),
      plans: this.getPlans()
    }).pipe(
      finalize(() => {
        this.loadingInProgress = false
        this.formGroup.enable()
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(({config, plans}) => {
      this.config = config
      this.plans = plans
    })

    this.onPlanChange();
  }

  copyPlanSubmit(): void {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    const plan = this.formGroup.value.plan as LibraryContainer
    if (plan.workoutsAmount > 100) {
      this.openWarningDialog(plan, () => this.copyPlan())
      return
    }
    this.copyPlan()
  }

  private copyPlan(): void {
    this.submitInProgress = true
    const plan = this.formGroup.value.plan
    const newName = this.formGroup.value.newName
    const newStartDate = this.formGroup.value.newStartDate
    const stepModifier = this.formGroup.value.stepModifier
    const direction = Platform.DIRECTION_TP_INT
    this.planClient.copyLibraryContainer(plan, newName, newStartDate, stepModifier, direction).pipe(
      finalize(() => this.submitInProgress = false),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe((response) => {
      this.notificationService.success(
        `Library name: ${response.planName}\nCopied workouts: ${response.workouts}`)
    })
  }

  private openWarningDialog(
    plan: LibraryContainer,
    continueCallback: () => void,
  ): void {
    const dialogRef = this.dialog.open(TpCopyPlanWarningDialogComponent, {
      data: plan,
    });

    dialogRef.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(result => {
        if (result) {
          continueCallback()
        }
      });
  }

  private getMonday(date: Date): Date {
    date = new Date(date);
    let day = date.getDay(),
      diff = date.getDate() - day + (day == 0 ? -6 : 1); // adjust when day is sunday
    return new Date(date.setDate(diff));
  }

  private getConfig() {
    return this.configurationClient.getConfig().pipe(
      map(config => config.config)
    )
  }

  private getPlans() {
    return this.planClient.getLibraries(Platform.TRAINING_PEAKS.key).pipe(
      map(plans => plans.map(plan => {
          return {name: plan.name, value: plan}
        })
      )
    )
  }

  private onPlanChange(): void {
    this.formGroup.controls['plan'].valueChanges.pipe(
    filter(value => !!value),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(value => {
      this.formGroup.patchValue({
        newName: value.name
      })
    })
  }
}
