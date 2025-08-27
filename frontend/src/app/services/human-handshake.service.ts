import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { fromEvent, merge, Subscription, timer } from 'rxjs';
import { mapTo, takeWhile } from 'rxjs/operators';

interface SeedResponse { seedId: string; expiresAt: number; }
interface ActivateResponse { token: string; expiresAt: number; }

const API_BASE = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class HumanHandshakeService {
  private seedId: string | null = null;
  private token: string | null = null;
  private subs: Subscription[] = [];
  private signals = new Set<string>();
  private activated = false;

  constructor(private http: HttpClient, private zone: NgZone) {}

  async initForAction(action: 'search' | 'checkout' | string): Promise<void> {
    const resp = await this.http.post<SeedResponse>(`${API_BASE}/api/hph/seed`, { action }).toPromise();
    this.seedId = resp?.seedId || null;
    this.activated = false;
    this.token = null;
    this.attachWatchers();
  }

  private attachWatchers() {
    this.detachWatchers();
    this.zone.runOutsideAngular(() => {
      const s1 = fromEvent(document, 'click').pipe(mapTo('click'));
      const s2 = fromEvent(window, 'scroll').pipe(mapTo('scroll'));
      const s3 = fromEvent(document, 'keydown').pipe(mapTo('keydown'));
      const s4 = fromEvent(document, 'focusin').pipe(mapTo('focus'));
      const dwell = timer(1500).pipe(mapTo('dwell'));
      const stream = merge(s1, s2, s3, s4, dwell).pipe(takeWhile(() => !this.activated));
      this.subs.push(stream.subscribe(async (sig: string) => {
        this.signals.add(sig);
        if (!this.activated && this.signals.size >= 3 && this.seedId) {
          try {
            const resp = await this.http.post<ActivateResponse>(`${API_BASE}/api/hph/activate`, { seedId: this.seedId }).toPromise();
            this.token = resp?.token || null;
            this.activated = !!this.token;
            this.detachWatchers();
          } catch {
            /* ignore */
          }
        }
      }));
    });
  }

  private detachWatchers() {
    this.subs.forEach(s => s.unsubscribe());
    this.subs = [];
  }

  getToken(): string | null { return this.token; }
  clear() { this.detachWatchers(); this.seedId = null; this.token = null; this.activated = false; this.signals.clear(); }
}
