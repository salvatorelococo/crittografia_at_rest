import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-password-dialog',
  templateUrl: './upload-dialog.component.html',
  styleUrls: ['./upload-dialog.component.scss']
})
export class UploadDialogComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<UploadDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) { }

  fileName = this.data.fileName;
  password: string;
  showDetails: boolean = false;

  ngOnInit() {
  }


  onStrengthChanged(strength: number) {
    console.log('password strength = ', strength);
  }

  addWithPassword() {
    this.dialogRef.close(this.password);
  }

  addFile() {
    this.dialogRef.close(-1);
  }

  close() {
    this.dialogRef.close(null);
  }
}
