import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface NodeInfo {
  nodeId: string;
  type: string;
  role?: string;
  status?: string;
  lastHeartbeat?: number;
}

export interface MetricRecord {
  nodeId: string;
  type: string;
  value: number;
  state: string;
  timestamp: number;
}

export interface ScalingEvent {
  type: string;
  action: string;
  reason: string;
  total: number;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly apiUrl = 'http://localhost:8085';

  constructor(private http: HttpClient) {}

  getNodes(): Observable<NodeInfo[]> {
    return this.http.get<NodeInfo[]>(`${this.apiUrl}/registry/nodes`);
  }

  getCluster(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.apiUrl}/scaling/cluster`);
  }

  getMetricsHistory(): Observable<MetricRecord[]> {
    return this.http.get<MetricRecord[]>(`${this.apiUrl}/metrics/history`);
  }

  getScalingEvents(): Observable<ScalingEvent[]> {
    return this.http.get<ScalingEvent[]>(`${this.apiUrl}/scaling/events`);
  }
}