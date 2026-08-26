from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
data=root/"data"/"index.json"
obj=json.loads(data.read_text(encoding="utf-8"))
if obj.get("schemaVersion")!=2 or not isinstance(obj.get("reports"),list): raise SystemExit("Invalid static index")
obj["databaseVersion"]="0.7.5"
obj["normalizerVersion"]=10
obj["currentProducer"]="OpenGLESScope 0.7.2"
obj["publishedOpenGlesSpec"]="OpenGL ES 3.2 (May 5, 2022)"
obj["publishedGlslEsSpec"]="GLSL ES 3.20 (August 14, 2023)"
obj["publishedEglSpec"]="EGL 1.5 (August 27, 2014)"
obj["registryAuditDate"]="2026-08-26"
obj["reports"].sort(key=lambda x:(str(x.get("submitted_at",x.get("submittedAt",""))),str(x.get("id",""))),reverse=True)
data.write_text(json.dumps(obj,separators=(",",":"),ensure_ascii=False)+"\n",encoding="utf-8")
print(f"Indexed {len(obj['reports'])} reports")
