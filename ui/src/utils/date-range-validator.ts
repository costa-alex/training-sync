import { AbstractControl, ValidationErrors } from '@angular/forms';

export function dateRangeValidator(
  startControlName = 'startDate',
  endControlName = 'endDate'
): (control: AbstractControl) => ValidationErrors | null {
  return (control: AbstractControl): ValidationErrors | null => {
    const startDate = control.get(startControlName)?.value;
    const endDate = control.get(endControlName)?.value;

    if (!startDate || !endDate) {
      return null;
    }

    return new Date(startDate).getTime() <= new Date(endDate).getTime()
      ? null
      : { invalidDateRange: true };
  };
}
