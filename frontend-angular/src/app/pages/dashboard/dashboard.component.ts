import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { interval, Subscription } from 'rxjs';
import { ApiService, MetricRecord, NodeInfo, ScalingEvent } from '../../services/api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit, OnDestroy {
  nodes: NodeInfo[] = [];
  metrics: MetricRecord[] = [];
  events: ScalingEvent[] = [];
  cluster: Record<string, number> = {};

  error = '';
  lastUpdate = '';

  private sub?: Subscription;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadAll();

    this.sub = interval(2000).subscribe(() => {
      this.loadAll();
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  loadAll(): void {
    this.api.getNodes().subscribe({
      next: data => {
        this.nodes = data || [];
        this.lastUpdate = new Date().toLocaleTimeString();
        this.error = '';
      },
      error: () => {
        this.error = 'No se pudo conectar con el API Gateway.';
      }
    });

    this.api.getCluster().subscribe({
      next: data => this.cluster = data || {},
      error: () => this.cluster = {}
    });

    this.api.getMetricsHistory().subscribe({
      next: data => this.metrics = (data || []).slice().reverse(),
      error: () => this.metrics = []
    });

    this.api.getScalingEvents().subscribe({
      next: data => this.events = (data || []).slice().reverse(),
      error: () => this.events = []
    });
  }

  get totalNodes(): number {
    return this.nodes.length;
  }

  get activeNodes(): number {
    return this.nodes.filter(n => this.normalize(n.status) === 'ACTIVE' || this.normalize(n.status) === 'ALIVE').length;
  }

  get criticalMetrics(): number {
    return this.metrics.filter(m => m.state === 'CRITICAL').length;
  }

  get latestMetrics(): MetricRecord[] {
    return this.metrics.slice(0, 12);
  }

  get latestEvents(): ScalingEvent[] {
    return this.events.slice(0, 10);
  }

  get clusterEntries(): { type: string; instances: number }[] {
    return Object.entries(this.cluster).map(([type, instances]) => ({ type, instances }));
  }

  get resourceGroups(): { type: string; nodes: NodeInfo[]; instances: number }[] {
    const types = new Set<string>();

    this.nodes.forEach(n => types.add(n.type));
    Object.keys(this.cluster).forEach(t => types.add(t));

    return Array.from(types).map(type => ({
      type,
      nodes: this.nodes.filter(n => n.type === type),
      instances: this.cluster[type] || 0
    }));
  }

  getStatusClass(status?: string): string {
    const s = this.normalize(status);

    if (s === 'ACTIVE' || s === 'ALIVE' || s === 'OK') return 'ok';
    if (s === 'SUSPECT' || s === 'WARNING') return 'warning';
    if (s === 'FAILED' || s === 'DOWN' || s === 'INACTIVE') return 'danger';

    return 'unknown';
  }

  getMetricClass(state: string): string {
    if (state === 'CRITICAL') return 'danger';
    if (state === 'LOW') return 'warning';
    return 'ok';
  }

  getEventClass(action: string): string {
    if (action.includes('SCALE_UP')) return 'up';
    if (action.includes('SCALE_DOWN')) return 'down';
    return 'blocked';
  }

  formatTime(value?: number): string {
    if (!value) return 'Sin dato';
    return new Date(value).toLocaleTimeString();
  }

  private normalize(value?: string): string {
    return (value || '').toUpperCase();
  }
}