#!/usr/bin/env bash
# =============================================================================
#  EtuBibliotheque - Lancement de la demo (Docker + Back Spring Boot + Front Angular)
#  Lance les services dans le bon ordre, attend qu'ils soient prets, ouvre le navigateur.
#  Usage : ./demo-start.sh   (depuis le dossier racine du projet)
# =============================================================================

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACK="$ROOT/Backend"
FRONT="$ROOT/Frontend"
LOG_DIR="$ROOT/.demo-logs"
mkdir -p "$LOG_DIR"

BACK_PID=""
FRONT_PID=""

# --- Couleurs ---
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# --- Nettoyage a la fermeture (Ctrl+C) ---
cleanup() {
    echo ""
    echo "Arret des services..."
    [ -n "$BACK_PID" ] && kill "$BACK_PID" 2>/dev/null || true
    [ -n "$FRONT_PID" ] && kill "$FRONT_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

test_url() {
    curl -sf -o /dev/null --max-time 5 "$1"
}

test_docker() {
    docker info > /dev/null 2>&1
}

echo ""
echo -e "${CYAN}==================================================${NC}"
echo -e "${CYAN}   EtuBibliotheque - Demarrage de la demo${NC}"
echo -e "${CYAN}==================================================${NC}"

# --- 1) Docker ---
echo -e "\n${YELLOW}[1/4] Verification de Docker...${NC}"
if test_docker; then
    echo -e "${GREEN}      Docker est deja demarre.${NC}"
else
    echo "      Docker n'est pas pret."
    echo -e "${RED}      ERREUR : demarre Docker Desktop (integration WSL2) puis relance ce script.${NC}"
    exit 1
fi

# --- 2) Back-end Spring Boot (port 8080) ---
echo -e "\n${YELLOW}[2/4] Back-end (http://localhost:8080)...${NC}"
if test_url "http://localhost:8080/actuator/health"; then
    echo -e "${GREEN}      Back-end deja en ligne.${NC}"
else
    echo "      Lancement du back-end en arriere-plan (MySQL demarre automatiquement)..."
    echo "      Logs : $LOG_DIR/back.log"
    (cd "$BACK" && ./mvnw spring-boot:run > "$LOG_DIR/back.log" 2>&1) &
    BACK_PID=$!

    echo "      Attente du demarrage (premier lancement = un peu plus long)..."
    DEADLINE=$(( $(date +%s) + 300 ))
    until test_url "http://localhost:8080/actuator/health"; do
        if [ "$(date +%s)" -gt "$DEADLINE" ]; then
            echo -e "${RED}      ERREUR : le back-end n'a pas demarre a temps (voir $LOG_DIR/back.log).${NC}"
            exit 1
        fi
        sleep 5
        echo "      ... attente de l'API"
    done
    echo -e "${GREEN}      Back-end pret (API 8080).${NC}"
fi

# --- 3) Front-end Angular (port 4200) ---
echo -e "\n${YELLOW}[3/4] Front-end (http://localhost:4200)...${NC}"
if test_url "http://localhost:4200"; then
    echo -e "${GREEN}      Front deja en ligne.${NC}"
else
    echo "      Lancement du front en arriere-plan..."
    echo "      Logs : $LOG_DIR/front.log"
    (cd "$FRONT" && npm start > "$LOG_DIR/front.log" 2>&1) &
    FRONT_PID=$!

    echo "      Attente de la compilation Angular..."
    DEADLINE=$(( $(date +%s) + 180 ))
    until test_url "http://localhost:4200"; do
        if [ "$(date +%s)" -gt "$DEADLINE" ]; then
            echo -e "${RED}      ERREUR : le front n'a pas demarre a temps (voir $LOG_DIR/front.log).${NC}"
            exit 1
        fi
        sleep 5
        echo "      ... attente du front"
    done
    echo -e "${GREEN}      Front pret (4200).${NC}"
fi

# --- 4) Ouverture du navigateur ---
echo -e "\n${YELLOW}[4/4] Ouverture du navigateur...${NC}"
if command -v explorer.exe > /dev/null 2>&1; then
    explorer.exe "http://localhost:4200" || true   # WSL2 : ouvre le navigateur Windows par defaut
elif command -v wslview > /dev/null 2>&1; then
    wslview "http://localhost:4200"
elif command -v xdg-open > /dev/null 2>&1; then
    xdg-open "http://localhost:4200"
else
    echo "      Ouvre manuellement : http://localhost:4200"
fi

echo -e "\n${GREEN}==================================================${NC}"
echo -e "${GREEN}   Tout est pret pour la demo !${NC}"
echo -e "${GREEN}   - Application : http://localhost:4200${NC}"
echo -e "${GREEN}   - 1er passage : cree un compte sur /register (remplir TOUS les champs)${NC}"
echo -e "${GREEN}   - Puis connecte-toi sur /login${NC}"
echo -e "${GREEN}==================================================${NC}"
echo ""
echo "(Ctrl+C dans ce terminal arrete le back et le front proprement.)"

# Garde le script actif tant que les services tournent, pour que le trap fonctionne
wait