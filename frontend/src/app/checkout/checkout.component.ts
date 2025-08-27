import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HumanHandshakeService } from '../services/human-handshake.service';

declare const grecaptcha: any;

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html'
})
export class CheckoutComponent implements OnInit {
  email = '';
  honeypot = '';
  siteKey = 'RECAPTCHA_V3_SITE_KEY';
  busy = false;
  error: string | null = null;

  constructor(private http: HttpClient, private hph: HumanHandshakeService) {}

  async ngOnInit() {
    await this.hph.initForAction('checkout');
  }

  async startCheckout() {
    this.error = null;
    if (this.honeypot) { this.error = 'Invalid submission.'; return; }
    const hphToken = this.hph.getToken();
    if (!hphToken) { this.error = 'Please interact with the page before continuing.'; return; }
    this.busy = true;
    try {
      const recaptchaToken = await grecaptcha.execute(this.siteKey, { action: 'checkout' });
      await this.http.post('/api/checkout/start', {
        email: this.email,
        recaptchaToken,
        action: 'checkout'
      }).toPromise();
    } catch (e: any) {
      this.error = e?.error?.message || 'Checkout blocked. Please try later.';
    } finally {
      this.busy = false;
    }
  }
}
