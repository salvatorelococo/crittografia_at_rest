import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-download-dialog',
  templateUrl: './download-dialog.component.html',
  styleUrls: ['./download-dialog.component.scss']
})
export class DownloadDialogComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<DownloadDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) { }

  fileName = this.data.fileName;
  password: string;
  hide = true;

  ngOnInit() {
  }

  download() {
    this.dialogRef.close(this.password);
  }

  close() {
    this.dialogRef.close();
  }

}
