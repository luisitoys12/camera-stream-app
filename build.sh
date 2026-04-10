#!/usr/bin/env bash
# =============================================================================
# build.sh — Compila CameraStream APK + AAB en GitHub Codespace
# Uso: bash build.sh
# =============================================================================
set -e
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()  { echo -e "${CYAN}[INFO]${NC}  $*"; }
ok()    { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

echo -e "${CYAN}"
echo "  ╔══════════════════════════════════╗"
echo "  ║   CameraStream — Build Script   ║"
echo "  ║   EstacionKUS  •  Irapuato GTO  ║"
echo "  ╚══════════════════════════════════╝"
echo -e "${NC}"

# ─── 1. Java 17 ─────────────────────────────────────────────────────────────
info "Verificando Java..."
if ! command -v java &>/dev/null || ! java -version 2>&1 | grep -q '17\|21'; then
    warn "Java 17 no encontrado. Instalando..."
    sudo apt-get update -qq
    sudo apt-get install -y -qq openjdk-17-jdk
fi
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
ok "Java: $(java -version 2>&1 | head -1)"

# ─── 2. Android SDK ──────────────────────────────────────────────────────────
info "Verificando Android SDK..."
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    warn "Android SDK no encontrado. Instalando command-line tools..."
    SDK_DIR="$HOME/android-sdk"
    mkdir -p "$SDK_DIR/cmdline-tools"
    CLI_ZIP="commandlinetools-linux-11076708_latest.zip"
    curl -fsSL "https://dl.google.com/android/repository/${CLI_ZIP}" -o "/tmp/${CLI_ZIP}"
    unzip -q "/tmp/${CLI_ZIP}" -d "$SDK_DIR/cmdline-tools"
    mv "$SDK_DIR/cmdline-tools/cmdline-tools" "$SDK_DIR/cmdline-tools/latest" 2>/dev/null || true
    export ANDROID_HOME="$SDK_DIR"
    export ANDROID_SDK_ROOT="$SDK_DIR"
    export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
    yes | sdkmanager --licenses &>/dev/null || true
    sdkmanager "platforms;android-35" "build-tools;35.0.0" &>/dev/null
    ok "Android SDK instalado en $SDK_DIR"
else
    export ANDROID_HOME="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
    ok "Android SDK: $ANDROID_HOME"
fi

# ─── 3. gradle-wrapper.jar ───────────────────────────────────────────────────
info "Verificando gradle-wrapper.jar..."
JAR_PATH="gradle/wrapper/gradle-wrapper.jar"
JAR_SIZE=$(wc -c < "$JAR_PATH" 2>/dev/null || echo 0)
if [ "$JAR_SIZE" -lt 40000 ]; then
    warn "gradle-wrapper.jar inválido ($JAR_SIZE bytes). Descargando Gradle 8.11.1..."
    curl -fsSL "https://services.gradle.org/distributions/gradle-8.11.1-bin.zip" \
        -o /tmp/gradle.zip
    # Combinar shared + main en un solo jar
    python3 - <<'PYEOF'
import zipfile, io

with open('/tmp/gradle.zip', 'rb') as f:
    outer = zipfile.ZipFile(f)
    shared = outer.read('gradle-8.11.1/lib/gradle-wrapper-shared-8.11.1.jar')
    main   = outer.read('gradle-8.11.1/lib/plugins/gradle-wrapper-main-8.11.1.jar')

buf = io.BytesIO()
with zipfile.ZipFile(buf, 'w', zipfile.ZIP_DEFLATED) as out:
    for name, data in [('shared', shared), ('main', main)]:
        with zipfile.ZipFile(io.BytesIO(data)) as z:
            for item in z.infolist():
                if name == 'main' and item.filename in ['META-INF/', 'META-INF/MANIFEST.MF', 'org/', 'org/gradle/', 'org/gradle/wrapper/']:
                    continue
                try:
                    out.writestr(item, z.read(item.filename))
                except Exception:
                    pass

with open('gradle/wrapper/gradle-wrapper.jar', 'wb') as f:
    f.write(buf.getvalue())
print(f'gradle-wrapper.jar: {len(buf.getvalue())} bytes')
PYEOF
fi
ok "gradle-wrapper.jar: $(wc -c < $JAR_PATH) bytes"
chmod +x gradlew

# ─── 4. Keystore ─────────────────────────────────────────────────────────────
info "Preparando keystore..."
if [ ! -f app/keystore.jks ]; then
    keytool -genkeypair -v \
        -keystore app/keystore.jks \
        -alias camerastream \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass camerastream123 -keypass camerastream123 \
        -dname "CN=CameraStream,OU=Dev,O=EstacionKUS,L=Irapuato,S=GTO,C=MX" 2>/dev/null
    ok "Keystore generado: app/keystore.jks"
else
    ok "Keystore existente: app/keystore.jks"
fi
export KEYSTORE_FILE=app/keystore.jks
export KEYSTORE_PASSWORD=camerastream123
export KEY_ALIAS=camerastream
export KEY_PASSWORD=camerastream123

# ─── 5. Tests ────────────────────────────────────────────────────────────────
info "Corriendo unit tests..."
./gradlew testDebugUnitTest --no-daemon -x lint -q && ok "Tests: PASS" || warn "Algunos tests fallaron — continuando build..."

# ─── 6. Build APK ────────────────────────────────────────────────────────────
info "Compilando APK release..."
./gradlew assembleRelease --no-daemon -x lint
APK=$(find app/build/outputs/apk/release -name '*.apk' | head -1)
[ -f "$APK" ] || error "APK no generado — revisa el log arriba"
ok "APK: $APK  ($(du -sh $APK | cut -f1))"

# ─── 7. Build AAB ────────────────────────────────────────────────────────────
info "Compilando AAB (Play Store)..."
./gradlew bundleRelease --no-daemon -x lint
AAB=$(find app/build/outputs/bundle/release -name '*.aab' | head -1)
[ -f "$AAB" ] || error "AAB no generado"
ok "AAB: $AAB  ($(du -sh $AAB | cut -f1))"

# ─── 8. Resumen ──────────────────────────────────────────────────────────────
V="2.0.0-$(date +'%Y%m%d')"
cp "$APK" "/tmp/CameraStream-v${V}.apk"
cp "$AAB" "/tmp/CameraStream-v${V}.aab"

echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║           ✅  BUILD EXITOSO                      ║${NC}"
echo -e "${GREEN}╠══════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║${NC}  APK → /tmp/CameraStream-v${V}.apk"
echo -e "${GREEN}║${NC}  AAB → /tmp/CameraStream-v${V}.aab"
echo -e "${GREEN}╠══════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║${NC}  Descarga el APK desde el explorador de archivos"
echo -e "${GREEN}║${NC}  o con:  cp /tmp/CameraStream-v${V}.apk ."
echo -e "${GREEN}╚══════════════════════════════════════════════════╝${NC}"
echo ""
