import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { LibraryClient } from 'infrastructure/client/library-client.service';
import { WorkoutClient } from 'infrastructure/client/workout.client';
import { NotificationService } from 'infrastructure/notification.service';
import { TrCopyLibraryToLibraryComponent } from './tr-copy-library-to-library.component';

describe('TrCopyLibraryToLibraryComponent', () => {
  it('does not submit an invalid form', () => {
    const workoutClient = jasmine.createSpyObj<WorkoutClient>(
      'WorkoutClient',
      {
        copyLibraryToLibrary: of(),
      }
    );

    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      providers: [
        { provide: WorkoutClient, useValue: workoutClient },
        { provide: LibraryClient, useValue: jasmine.createSpyObj<LibraryClient>('LibraryClient', ['getLibraries']) },
        { provide: NotificationService, useValue: jasmine.createSpyObj<NotificationService>('NotificationService', ['success']) },
      ],
    });

    const component = TestBed.createComponent(TrCopyLibraryToLibraryComponent).componentInstance;

    component.copyWorkoutSubmit();

    expect(workoutClient.copyLibraryToLibrary).not.toHaveBeenCalled();
    expect(component.submitInProgress).toBeFalse();
  });
});
