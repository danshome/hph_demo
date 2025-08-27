import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CheckoutComponent } from './checkout/checkout.component';
import { HphInterceptor } from './interceptors/hph-interceptor';

@NgModule({
  declarations: [CheckoutComponent],
  imports: [BrowserModule, HttpClientModule, FormsModule],
  providers: [{ provide: HTTP_INTERCEPTORS, useClass: HphInterceptor, multi: true }],
  bootstrap: [CheckoutComponent]
})
export class AppModule {}
