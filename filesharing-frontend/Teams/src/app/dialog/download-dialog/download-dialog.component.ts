import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-download-dialog',
  templateUrl: './download-dialog.component.html',
  styleUrls: ['./download-dialog.component.scss']
})
export class DownloadDialogComponent implements OnInit {

  password: string;
  hide = true;

  constructor(public dialogRef: MatDialogRef<DownloadDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
  }

  download() {
    this.dialogRef.close(this.password);
  }

}
