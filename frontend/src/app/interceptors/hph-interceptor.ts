import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HumanHandshakeService } from '../services/human-handshake.service';

@Injectable()
export class HphInterceptor implements HttpInterceptor {
  constructor(private hph: HumanHandshakeService) {}
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const needsHPH = /\/api\/(checkout|orders)/.test(req.url);
    const token = this.hph.getToken();
    if (needsHPH && token) {
      const cloned = req.clone({ setHeaders: { 'X-HPH': token }});
      return next.handle(cloned);
    }
    return next.handle(req);
  }
}
