import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'extensionCrypt'
})
export class ExtensionCryptPipe implements PipeTransform {

  transform(value: string): string {
    if(value){
      var splitted: string[] = value.split(".");
      if(splitted[splitted.length-1] == 'crypt'){
        return splitted.slice(0,splitted.length-1).join(".");
      }
    }
    return value;
  }

}
