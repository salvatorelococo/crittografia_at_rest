import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {BucketService} from "../../services/bucket.service";
import {ActivatedRoute, UrlSegment} from "@angular/router";
import {ResourceDTO, TeamDTO} from "../../models/models";
import {ResourceService} from "../../services/resource.service";
import {SYNC_TYPE, SyncService} from "../../services/sync.service";
import {TeamService} from "../../services/team.service";
import {UploadProgressModel} from "../../models/UploadProgressModel";
import {HttpEventType} from "@angular/common/http";
import {MatDialog} from "@angular/material/dialog";
import {DownloadDialogComponent} from "../../dialog/download-dialog/download-dialog.component";
import {UploadDialogComponent} from "../../dialog/upload-dialog/upload-dialog.component";
import {MatSnackBar, MatSnackBarConfig} from "@angular/material/snack-bar";

class PathDescriptor{
  path: string;
  uniqueId: string;
}

@Component({
  selector: 'app-bucket-detail',
  templateUrl: './bucket-detail.component.html',
  styleUrls: ['./bucket-detail.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class BucketDetailComponent implements OnInit {

  public files: ResourceDTO;
  private originalResources: ResourceDTO;
  public team: string;
  public bucket: string;
  public teamDTO: TeamDTO;
  public urlparams: UrlSegment[] = [];
  public folderList: PathDescriptor[];
  public showLoading: boolean = false;
  public percentage: number;
  public currentAction: string = 'Nessuna azione';

  constructor(private teamService: TeamService,
              private bucketService: BucketService,
              private resourceService: ResourceService,
              private syncService: SyncService,
              private router: ActivatedRoute,
              public snackbar: MatSnackBar,
              public dialog: MatDialog) {
  }

  ngOnInit() {
    this.router.paramMap.subscribe(params=>{
      console.log("ROUTE PARAMS");
      this.team = params.get('team');
      this.bucket = params.get('bucket');
      this.loadResource();
      this.syncService.register().subscribe((type: SYNC_TYPE)=>{
        console.log("Sync", type);
        if(type == SYNC_TYPE.Resource){
          this.loadResource();
        }
      });
    });
    this.router.url.subscribe((data) => {
      console.log('params ', data); //contains all the segments so put logic here of determining what to do according to nesting depth
      this.urlparams = data;
      this.navigateFolder(this.originalResources);
    });

  }

  loadResource(){
    this.teamDTO = this.teamService.getTeamByUUID(this.team);
    this.resourceService.get(this.team, this.bucket).subscribe(data =>{
      this.originalResources = data;
      this.navigateFolder(data);
    });
  }

  private navigateFolder(data: ResourceDTO){
    this.folderList = [];
    if(this.urlparams && this.urlparams.length > 0 && data) {
      let find = data;
      for (let folder of this.urlparams) {
        find = find.childs.find(x=>{return x.uniqueKey == folder.path});
        if(find) {
          this.folderList.push({path: find.name, uniqueId: folder.path});
        }else{
          console.log("non ho trovato la folder "+folder, find);
        }
      }
      this.files = find;
    }else{
      this.files = data;
    }
  }

  deleteAttachment(index) {
  }

  uploadFileEvent(file: File){
      const dialogRef = this.dialog.open(UploadDialogComponent, {
        width: '50vw',
        data: {fileName: file.name}
      });
      dialogRef.afterClosed().subscribe((password) => {
        if(typeof password == 'number') {
          this.uploadFile(file, null);
        }
        else if(password != null){
          this.uploadFile(file, password);
        }
      });
  }

  openSnackBar(message: string, action: string = null) {
    let config = new MatSnackBarConfig();
    config.verticalPosition = "bottom";
    config.horizontalPosition = "center";
    config.duration = 3000;
    this.snackbar.open(message, action ? action : undefined, config);
  }

  private uploadFile(file: File, password: string = null) {
    this.currentAction = "Upload";
    this.percentage = 0;
    this.resourceService.addContent(this.team, this.bucket, this.urlparams.length > 0 ? this.urlparams[this.urlparams.length - 1].path : null, file, password).subscribe((data: UploadProgressModel) => {
          console.log(data);
          if (data.status == HttpEventType.Response.toString()) {
            this.syncService.sendEvent(SYNC_TYPE.Resource);
            this.showLoading = false;
          }
          else {
            this.percentage = data.percentage;
            this.showLoading = true;
          }
        },
        (error) => {
          this.showLoading = false;
          this.openSnackBar('Errore nel caricamento del file!', 'Chiudi');
        });
  }

  download(file: ResourceDTO) {
    this.currentAction = "Download";
    /* Viene mostrato lo spinner fin tanto che non viene caricato il file o restituito errore */
    this.showLoading = true;
    this.resourceService.download(this.team, this.bucket, file.uniqueKey).subscribe(()=>{
      this.showLoading = false;
      },
        (error) => {
      this.showLoading = false;
      this.openSnackBar('Errore nel download del file!', 'Chiudi');
    });
  }

  downloadCrypt(file: ResourceDTO) {
    const dialogRef = this.dialog.open(DownloadDialogComponent, {
      width: '50vw',
      data: {fileName: file.name}
    });
    dialogRef.afterClosed().subscribe((password: string) => {
        /* password == null quando si annulla il download */
        if (password == null) {
            return null;
        }
        /* Viene mostrato lo spinner fin tanto che non viene caricato il file o restituito un errore */
        this.currentAction = "Download";
        this.showLoading = true;
        this.resourceService.downloadCrypt(this.team, this.bucket, file.uniqueKey, password).subscribe(()=>{
            this.showLoading = false;
            },
            (error) => {
            this.showLoading = false;
            this.openSnackBar('Errore nel download: Password errata!', 'Chiudi');
          });
    });
  }

  getPathForLink(index){
    return this.urlparams.slice(0,index+1).reduce((initial, item)=>{initial.push(item.path); return initial},[]).join('/');
  }
}
