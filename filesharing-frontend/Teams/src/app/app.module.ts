import { BrowserModule } from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule} from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import { DragDropDirectiveDirective } from './directive/drag-drop-directive.directive';
import { UploadFileComponent } from './component/upload-file/upload-file.component';
import {
    _MatMenuDirectivesModule,
    MatButtonModule,
    MatCardModule, MatDialogModule,
    MatExpansionModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatMenuModule,
    MatToolbarModule,
    MatTooltipModule
} from "@angular/material";
import { DashboardComponent } from './views/dashboard/dashboard.component';
import { RegistrationComponent } from './views/registration/registration.component';
import { LoginComponent } from './views/login/login.component';
import { TeamsComponent } from './views/teams/teams.component';
import { TeamDialogComponent } from './dialog/team-dialog/team-dialog.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import { BucketDialogComponent } from './dialog/bucket-dialog/bucket-dialog.component';
import { BucketsComponent } from './views/buckets/buckets.component';
import { BucketDetailComponent } from './views/bucket-detail/bucket-detail.component';
import {KeycloakAngularModule, KeycloakService} from "keycloak-angular";
import {initializer} from "./app-init";
import {HttpClientModule} from "@angular/common/http";
import { FolderDialogComponent } from './dialog/folder-dialog/folder-dialog.component';
import { FoldersComponent } from './views/folders/folders.component';
import { MemberPipe } from './pipe/member.pipe';
import { UploadDialogComponent } from './dialog/upload-dialog/upload-dialog.component';
import { DownloadDialogComponent } from './dialog/download-dialog/download-dialog.component';
import { ExtensionCryptPipe } from './pipe/extension-crypt.pipe';
import { MatPasswordStrengthModule } from '@angular-material-extensions/password-strength';
import {MatSnackBarModule} from "@angular/material/snack-bar";

@NgModule({
  declarations: [
    AppComponent,
    DragDropDirectiveDirective,
    UploadFileComponent,
    DashboardComponent,
    RegistrationComponent,
    LoginComponent,
    TeamsComponent,
    TeamDialogComponent,
    BucketDialogComponent,
    BucketsComponent,
    BucketDetailComponent,
    FolderDialogComponent,
    FoldersComponent,
    MemberPipe,
    UploadDialogComponent,
    DownloadDialogComponent,
    ExtensionCryptPipe
  ],
    imports: [
        HttpClientModule,
        BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        MatToolbarModule,
        MatButtonModule,
        MatIconModule,
        MatExpansionModule,
        MatCardModule,
        MatTooltipModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        FormsModule,
        ReactiveFormsModule,
        KeycloakAngularModule,
        _MatMenuDirectivesModule,
        MatMenuModule,
        MatPasswordStrengthModule.forRoot(),
        MatSnackBarModule/**/
    ],
  providers:  [    {
    provide: APP_INITIALIZER,
    useFactory: initializer,
    multi: true,
    deps: [KeycloakService]
  }],
  entryComponents:[
    TeamDialogComponent,
    BucketDialogComponent,
    FolderDialogComponent,
    UploadDialogComponent,
    DownloadDialogComponent
  ],
  bootstrap: [
      AppComponent,
      BucketDetailComponent/**/
  ]
})
export class AppModule { }
