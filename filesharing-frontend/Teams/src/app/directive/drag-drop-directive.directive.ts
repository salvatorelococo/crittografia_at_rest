import {Directive, EventEmitter, HostBinding, HostListener, Output} from '@angular/core';
import {UploadDialogComponent} from "../dialog/upload-dialog/upload-dialog.component"; // TODO: Aggiunto
import {MatDialog} from "@angular/material/dialog"; // TODO: Aggiunto

@Directive({
  selector: '[appDragDrop]'
})
export class DragDropDirectiveDirective {

  constructor(/* TODO: Aggiunto */ public dialog: MatDialog){}

  @Output() onFileDropped = new EventEmitter<any>();

  @HostBinding('style.background-color') private background;// = '#f5fcff'
  @HostBinding('style.opacity') private opacity = '1';

  //Dragover listener
  @HostListener('dragover', ['$event']) onDragOver(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.background = '#9ecbec';
    this.opacity = '0.8';
  }

  //Dragleave listener
  @HostListener('dragleave', ['$event']) public onDragLeave(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.background = null; //'#f5fcff'
    this.opacity = '1';
  }

  //Drop listener
  @HostListener('drop', ['$event']) public ondrop(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.background = null;
    this.opacity = '1';
    this.openDialogPassword(); // TODO: Aggiunto
    // TODO: Aggiornare condizione con verifica password
    let files = evt.dataTransfer.files;
    if (files.length > 0) {
      this.onFileDropped.emit(files)
    }
  }

  // TODO: Aggiunto
  openDialogPassword(): void {
    const dialogRef = this.dialog.open(UploadDialogComponent, {
      width: '50vw',
      data: {}
    });
    dialogRef.afterClosed().subscribe((result:string) => {
      // TODO: Azione post-chiusura
    });
  }
}