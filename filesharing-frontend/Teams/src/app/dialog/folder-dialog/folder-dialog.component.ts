import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {FolderDTO} from "../../models/models";

@Component({
  selector: 'app-folder-dialog',
  templateUrl: './folder-dialog.component.html',
  styleUrls: ['./folder-dialog.component.scss']
})
export class FolderDialogComponent implements OnInit {

  public folder: FolderDTO = new class implements FolderDTO {
    name: string;
    parentUniqueId: string;
  };

  constructor(public dialogRef: MatDialogRef<FolderDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
  }

  addFolder(){
    this.dialogRef.close(this.folder);
  }

}
