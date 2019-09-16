import {Directive, EventEmitter, HostBinding, HostListener, Output} from '@angular/core';
import {UploadDialogComponent} from "../dialog/upload-dialog/upload-dialog.component";
import {MatDialog} from "@angular/material/dialog";

@Directive({
  selector: '[appDragDrop]'
})
export class DragDropDirectiveDirective {

  constructor(public dialog: MatDialog){}

  @Output() onFileDropped = new EventEmitter<any>();

  @HostBinding('style.background-color') private background;// = '#f5fcff'
  @HostBinding('style.opacity') private opacity = '1'

  //Dragover listener
  @HostListener('dragover', ['$event']) onDragOver(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.background = '#9ecbec';
    this.opacity = '0.8'
  }

  //Dragleave listener
  @HostListener('dragleave', ['$event']) public onDragLeave(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.background = null; //'#f5fcff'
    this.opacity = '1'
  }

  //Drop listener
  @HostListener('drop', ['$event']) public ondrop(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.background = null;
    this.opacity = '1'
    this.openDialogPassword();
    // TODO: Aggiornare condizione con verifica password
    if(false) {
      let files = evt.dataTransfer.files;
      if (files.length > 0) {
        this.onFileDropped.emit(files)
      }
    }
  }

  // TODO: Rimuovere metodo
  openDialogPassword(): void {
    const dialogRef = this.dialog.open(UploadDialogComponent, {
      width: '50vw',
      data: {}
    });
    dialogRef.afterClosed().subscribe((result:string) => {

      });
  }
}