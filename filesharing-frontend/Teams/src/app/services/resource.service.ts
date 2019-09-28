import {EventEmitter, Injectable} from '@angular/core';
import {HttpClient, HttpEventType, HttpResponse} from "@angular/common/http";
import {FolderDTO, ResourceDTO} from "../models/models";
import {environment} from "../../environments/environment";
import {UploadProgressModel} from "../models/UploadProgressModel";
import {share} from "rxjs/operators";
import {UploadService} from "../models/UploadService";

@Injectable({
  providedIn: 'root'
})
export class ResourceService implements UploadService{

  private baseUrl: string = environment.apiBaseUrl+"/resource";

  constructor(private httpClient: HttpClient) { }

  public get(uuid: string, bucket: string){
    return this.httpClient.get<ResourceDTO>(this.baseUrl+'/'+uuid+'/'+bucket);
  }

  public addFolder(uuid: string, bucket: string, folder: FolderDTO){
    return this.httpClient.post(this.baseUrl+'/addFolder/'+uuid+'/'+bucket, folder);
  }

  public addContent(uuid: string, bucketName: string, parentUniqueId: string, file: File, password: string = null){
    return this.upload(this.baseUrl+'/addContent/'+uuid+'/'+bucketName, {parentUniqueId:parentUniqueId, file: file, password: password});
  }

  public download(uuid: string, bucketName: string, uniqueId: string){
    let reg = this.httpClient.post(this.baseUrl+'/'+uuid+'/'+bucketName+'/'+uniqueId, {}, {responseType: 'arraybuffer', observe: 'response'}).pipe(share());
    reg.subscribe(res => {
      return this.downLoadFile(res);
    });
    return reg;
  }

  public downloadCrypt(uuid: string, bucketName: string, uniqueId: string, password: string = null){
    if(password == null){
      return null;
    }

    let reg = this.httpClient.post(this.baseUrl+'/'+uuid+'/'+bucketName+'/'+uniqueId, {"password" : password}, {responseType: 'arraybuffer', observe: 'response'}).pipe(share());

    reg.subscribe(res => {
      return this.downLoadFile(res);
    });
    return reg;
  }

  private downLoadFile(data: any) {
    var blob = new Blob([data.body], {type: data.headers.get('content-type')});
    const element = document.createElement('a');
    element.href = URL.createObjectURL(blob);
    const contentDispositionHeader: string = data.headers.get('content-disposition'); // <== Getting error here, Not able to read Response Headers
    const parts: string[] = contentDispositionHeader.split(';');
    const filename = parts[1].split('=')[1].replace(/\"/g, "");
    element.download = filename;
    // element.target = "_blank";
    let elem = document.body.appendChild(element);
    element.click();
    console.log(elem, element);
    document.body.removeChild(elem);
  }

  upload(url: string, form: any): EventEmitter<UploadProgressModel> {
    let uploadAndProgressObservable: EventEmitter<UploadProgressModel> = new EventEmitter();
    var formData = new FormData();
    for (let key in form) {
      if (form[key] && !({}.toString.call(form[key]) === '[object Function]')){
        formData.append(key, form[key]);
      }
    }
    let uploadProgressModel = new UploadProgressModel();
    uploadProgressModel.filename = form.filename;
    uploadProgressModel.field = form.field;
    let post = this.httpClient.post(url, formData, {reportProgress: true, observe: 'events'}).pipe(share());
    post.subscribe(event => {
          uploadProgressModel.status = event.type.toString();
          if (event.type === HttpEventType.UploadProgress) {
            let percentDone = Math.round(100 * event.loaded / event.total);
            uploadProgressModel.percentage = percentDone;
            uploadAndProgressObservable.emit(uploadProgressModel);
          } else if (event instanceof HttpResponse) {
            uploadAndProgressObservable.emit(uploadProgressModel);
          }
        },error=>{uploadAndProgressObservable.error(error);},
        ()=>{uploadAndProgressObservable.complete();console.log(form.file.name+" COMPLETE");});

    return uploadAndProgressObservable;
  }

}
