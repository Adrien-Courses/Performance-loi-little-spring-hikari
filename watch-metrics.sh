#!/usr/bin/env bash
# Affiche toutes les secondes les trois metriques de l'article.
# Usage : ./watch-metrics.sh
BASE=http://localhost:8080/actuator/metrics
val() { curl -s "$BASE/$1" | sed -n 's/.*"value":\([0-9.E+-]*\).*/\1/p' | head -1; }
printf "%-10s %-14s %-16s %-16s %-18s\n" time tomcat.busy hikari.active hikari.pending hikari.usage.mean
while true; do
  busy=$(val tomcat.threads.busy)
  active=$(val hikaricp.connections.active)
  pending=$(val hikaricp.connections.pending)
  usage=$(curl -s "$BASE/hikaricp.connections.usage" | python3 -c '
import sys,json
d=json.load(sys.stdin)["measurements"]
m={x["statistic"]:x["value"] for x in d}
print("%.1f ms" % (1000*m["TOTAL_TIME"]/m["COUNT"]) if m.get("COUNT") else "n/a")')
  printf "%-10s %-14s %-16s %-16s %-18s\n" "$(date +%T)" "$busy" "$active" "$pending" "$usage"
  sleep 1
done
