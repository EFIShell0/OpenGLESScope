#include <jni.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl32.h>
#include <algorithm>
#include <cctype>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>

static std::string esc(const std::string& s) {
    std::ostringstream o;
    for (unsigned char c : s) {
        switch (c) {
            case '"': o << "\\\""; break;
            case '\\': o << "\\\\"; break;
            case '\b': o << "\\b"; break;
            case '\f': o << "\\f"; break;
            case '\n': o << "\\n"; break;
            case '\r': o << "\\r"; break;
            case '\t': o << "\\t"; break;
            default:
                if (c < 0x20) o << "\\u" << std::hex << std::setw(4) << std::setfill('0') << static_cast<int>(c) << std::dec;
                else o << c;
        }
    }
    return o.str();
}

static std::string q(const char* s) { return std::string("\"") + esc(s ? s : "") + "\""; }
static std::string q(const std::string& s) { return std::string("\"") + esc(s) + "\""; }
static std::string hexv(EGLint v) { std::ostringstream o; o << "0x" << std::uppercase << std::hex << static_cast<unsigned int>(v); return o.str(); }


static std::string enumDisplay(GLint value, const std::string& category) {
    if (category == "compressedFormats") {
        switch (static_cast<GLenum>(value)) {
            case GL_COMPRESSED_R11_EAC: return "GL_COMPRESSED_R11_EAC (" + hexv(value) + ")";
            case GL_COMPRESSED_SIGNED_R11_EAC: return "GL_COMPRESSED_SIGNED_R11_EAC (" + hexv(value) + ")";
            case GL_COMPRESSED_RG11_EAC: return "GL_COMPRESSED_RG11_EAC (" + hexv(value) + ")";
            case GL_COMPRESSED_SIGNED_RG11_EAC: return "GL_COMPRESSED_SIGNED_RG11_EAC (" + hexv(value) + ")";
            case GL_COMPRESSED_RGB8_ETC2: return "GL_COMPRESSED_RGB8_ETC2 (" + hexv(value) + ")";
            case GL_COMPRESSED_SRGB8_ETC2: return "GL_COMPRESSED_SRGB8_ETC2 (" + hexv(value) + ")";
            case GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2: return "GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 (" + hexv(value) + ")";
            case GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2: return "GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 (" + hexv(value) + ")";
            case GL_COMPRESSED_RGBA8_ETC2_EAC: return "GL_COMPRESSED_RGBA8_ETC2_EAC (" + hexv(value) + ")";
            case GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC: return "GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC (" + hexv(value) + ")";
            default: break;
        }
        if (value == 0x87F9) return "GL_3DC_X_AMD (" + hexv(value) + ")";
        if (value == 0x87FA) return "GL_3DC_XY_AMD (" + hexv(value) + ")";
        if (value == 0x8C92) return "GL_ATC_RGB_AMD (" + hexv(value) + ")";
        if (value == 0x8C93) return "GL_ATC_RGBA_EXPLICIT_ALPHA_AMD (" + hexv(value) + ")";
        if (value == 0x87EE) return "GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD (" + hexv(value) + ")";
        if (value == 0x8D64) return "GL_ETC1_RGB8_OES (" + hexv(value) + ")";
        if (value == 0x8B90) return "GL_PALETTE4_RGB8_OES (" + hexv(value) + ")";
        if (value == 0x8B91) return "GL_PALETTE4_RGBA8_OES (" + hexv(value) + ")";
        if (value == 0x8B92) return "GL_PALETTE4_R5_G6_B5_OES (" + hexv(value) + ")";
        if (value == 0x8B93) return "GL_PALETTE4_RGBA4_OES (" + hexv(value) + ")";
        if (value == 0x8B94) return "GL_PALETTE4_RGB5_A1_OES (" + hexv(value) + ")";
        if (value == 0x8B95) return "GL_PALETTE8_RGB8_OES (" + hexv(value) + ")";
        if (value == 0x8B96) return "GL_PALETTE8_RGBA8_OES (" + hexv(value) + ")";
        if (value == 0x8B97) return "GL_PALETTE8_R5_G6_B5_OES (" + hexv(value) + ")";
        if (value == 0x8B98) return "GL_PALETTE8_RGBA4_OES (" + hexv(value) + ")";
        if (value == 0x8B99) return "GL_PALETTE8_RGB5_A1_OES (" + hexv(value) + ")";
        if (value == 0x83F0) return "GL_COMPRESSED_RGB_S3TC_DXT1_EXT (" + hexv(value) + ")";
        if (value == 0x83F1) return "GL_COMPRESSED_RGBA_S3TC_DXT1_EXT (" + hexv(value) + ")";
        if (value == 0x83F2) return "GL_COMPRESSED_RGBA_S3TC_DXT3_EXT (" + hexv(value) + ")";
        if (value == 0x83F3) return "GL_COMPRESSED_RGBA_S3TC_DXT5_EXT (" + hexv(value) + ")";
        if (value == 0x8C00) return "GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG (" + hexv(value) + ")";
        if (value == 0x8C01) return "GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG (" + hexv(value) + ")";
        if (value == 0x8C02) return "GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG (" + hexv(value) + ")";
        if (value == 0x8C03) return "GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG (" + hexv(value) + ")";
        if (value == 0x8C70) return "GL_COMPRESSED_LUMINANCE_LATC1_NV (" + hexv(value) + ")";
        if (value == 0x8C71) return "GL_COMPRESSED_SIGNED_LUMINANCE_LATC1_NV (" + hexv(value) + ")";
        if (value == 0x8C72) return "GL_COMPRESSED_LUMINANCE_ALPHA_LATC2_NV (" + hexv(value) + ")";
        if (value == 0x8C73) return "GL_COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_NV (" + hexv(value) + ")";
        if (value == 0x9137) return "GL_COMPRESSED_RGBA_PVRTC_2BPPV2_IMG (" + hexv(value) + ")";
        if (value == 0x9138) return "GL_COMPRESSED_RGBA_PVRTC_4BPPV2_IMG (" + hexv(value) + ")";
        if (value == 0x93B0) return "GL_COMPRESSED_RGBA_ASTC_4x4_KHR (" + hexv(value) + ")";
        if (value == 0x93B1) return "GL_COMPRESSED_RGBA_ASTC_5x4_KHR (" + hexv(value) + ")";
        if (value == 0x93B2) return "GL_COMPRESSED_RGBA_ASTC_5x5_KHR (" + hexv(value) + ")";
        if (value == 0x93B3) return "GL_COMPRESSED_RGBA_ASTC_6x5_KHR (" + hexv(value) + ")";
        if (value == 0x93B4) return "GL_COMPRESSED_RGBA_ASTC_6x6_KHR (" + hexv(value) + ")";
        if (value == 0x93B5) return "GL_COMPRESSED_RGBA_ASTC_8x5_KHR (" + hexv(value) + ")";
        if (value == 0x93B6) return "GL_COMPRESSED_RGBA_ASTC_8x6_KHR (" + hexv(value) + ")";
        if (value == 0x93B7) return "GL_COMPRESSED_RGBA_ASTC_8x8_KHR (" + hexv(value) + ")";
        if (value == 0x93B8) return "GL_COMPRESSED_RGBA_ASTC_10x5_KHR (" + hexv(value) + ")";
        if (value == 0x93B9) return "GL_COMPRESSED_RGBA_ASTC_10x6_KHR (" + hexv(value) + ")";
        if (value == 0x93BA) return "GL_COMPRESSED_RGBA_ASTC_10x8_KHR (" + hexv(value) + ")";
        if (value == 0x93BB) return "GL_COMPRESSED_RGBA_ASTC_10x10_KHR (" + hexv(value) + ")";
        if (value == 0x93BC) return "GL_COMPRESSED_RGBA_ASTC_12x10_KHR (" + hexv(value) + ")";
        if (value == 0x93BD) return "GL_COMPRESSED_RGBA_ASTC_12x12_KHR (" + hexv(value) + ")";
        if (value == 0x93D0) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR (" + hexv(value) + ")";
        if (value == 0x93D1) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR (" + hexv(value) + ")";
        if (value == 0x93D2) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR (" + hexv(value) + ")";
        if (value == 0x93D3) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR (" + hexv(value) + ")";
        if (value == 0x93D4) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR (" + hexv(value) + ")";
        if (value == 0x93D5) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR (" + hexv(value) + ")";
        if (value == 0x93D6) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR (" + hexv(value) + ")";
        if (value == 0x93D7) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR (" + hexv(value) + ")";
        if (value == 0x93D8) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR (" + hexv(value) + ")";
        if (value == 0x93D9) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR (" + hexv(value) + ")";
        if (value == 0x93DA) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR (" + hexv(value) + ")";
        if (value == 0x93DB) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR (" + hexv(value) + ")";
        if (value == 0x93DC) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR (" + hexv(value) + ")";
        if (value == 0x93DD) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR (" + hexv(value) + ")";
        if (value == 0x93C0) return "GL_COMPRESSED_RGBA_ASTC_3x3x3_OES (" + hexv(value) + ")";
        if (value == 0x93C1) return "GL_COMPRESSED_RGBA_ASTC_4x3x3_OES (" + hexv(value) + ")";
        if (value == 0x93C2) return "GL_COMPRESSED_RGBA_ASTC_4x4x3_OES (" + hexv(value) + ")";
        if (value == 0x93C3) return "GL_COMPRESSED_RGBA_ASTC_4x4x4_OES (" + hexv(value) + ")";
        if (value == 0x93C4) return "GL_COMPRESSED_RGBA_ASTC_5x4x4_OES (" + hexv(value) + ")";
        if (value == 0x93C5) return "GL_COMPRESSED_RGBA_ASTC_5x5x4_OES (" + hexv(value) + ")";
        if (value == 0x93C6) return "GL_COMPRESSED_RGBA_ASTC_5x5x5_OES (" + hexv(value) + ")";
        if (value == 0x93C7) return "GL_COMPRESSED_RGBA_ASTC_6x5x5_OES (" + hexv(value) + ")";
        if (value == 0x93C8) return "GL_COMPRESSED_RGBA_ASTC_6x6x5_OES (" + hexv(value) + ")";
        if (value == 0x93C9) return "GL_COMPRESSED_RGBA_ASTC_6x6x6_OES (" + hexv(value) + ")";
        if (value == 0x93E0) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_3x3x3_OES (" + hexv(value) + ")";
        if (value == 0x93E1) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x3x3_OES (" + hexv(value) + ")";
        if (value == 0x93E2) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4x3_OES (" + hexv(value) + ")";
        if (value == 0x93E3) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4x4_OES (" + hexv(value) + ")";
        if (value == 0x93E4) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4x4_OES (" + hexv(value) + ")";
        if (value == 0x93E5) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5x4_OES (" + hexv(value) + ")";
        if (value == 0x93E6) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5x5_OES (" + hexv(value) + ")";
        if (value == 0x93E7) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5x5_OES (" + hexv(value) + ")";
        if (value == 0x93E8) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6x5_OES (" + hexv(value) + ")";
        if (value == 0x93E9) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6x6_OES (" + hexv(value) + ")";
    }
    if (category == "shaderBinaryFormats") {
        if (value == 0x8C0A) return "GL_SGX_BINARY_IMG (" + hexv(value) + ")";
        if (value == 0x8F60) return "GL_MALI_SHADER_BINARY_ARM (" + hexv(value) + ")";
        if (value == 0x8FC4) return "GL_SHADER_BINARY_VIV (" + hexv(value) + ")";
        if (value == 0x9250) return "GL_SHADER_BINARY_DMP (" + hexv(value) + ")";
        if (value == 0x9260) return "GL_GCCSO_SHADER_BINARY_FJ (" + hexv(value) + ")";
        if (value == 0x890B) return "GL_NVIDIA_PLATFORM_BINARY_NV (" + hexv(value) + ")";
    }
    if (category == "programBinaryFormats") {
        if (value == 0x8740) return "GL_Z400_BINARY_AMD (" + hexv(value) + ")";
        if (value == 0x8F61) return "GL_MALI_PROGRAM_BINARY_ARM (" + hexv(value) + ")";
        if (value == 0x9130) return "GL_SGX_PROGRAM_BINARY_IMG (" + hexv(value) + ")";
        if (value == 0x93A6) return "GL_PROGRAM_BINARY_ANGLE (" + hexv(value) + ")";
    }
    return hexv(value);
}

static std::vector<std::string> splitExt(const char* s) {
    std::vector<std::string> out;
    if (!s) return out;
    std::istringstream in(s);
    std::string x;
    while (in >> x) {
        if (std::find(out.begin(), out.end(), x) == out.end()) out.push_back(x);
    }
    return out;
}

static bool hasExt(const std::vector<std::string>& extensions, const char* name) {
    return std::find(extensions.begin(), extensions.end(), std::string(name)) != extensions.end();
}

static void appendStringArray(std::ostringstream& o, const std::vector<std::string>& v) {
    o << '[';
    for (size_t i = 0; i < v.size(); ++i) {
        if (i) o << ',';
        o << q(v[i]);
    }
    o << ']';
}

static std::pair<int, int> parseGlVersion(const char* s) {
    if (!s) return {0, 0};
    const std::string v(s);
    const auto p = v.find("OpenGL ES ");
    if (p == std::string::npos) return {0, 0};
    size_t i = p + 10;
    while (i < v.size() && std::isspace(static_cast<unsigned char>(v[i]))) ++i;
    int major = 0;
    int minor = 0;
    while (i < v.size() && std::isdigit(static_cast<unsigned char>(v[i]))) major = major * 10 + (v[i++] - '0');
    if (i < v.size() && v[i] == '.') ++i;
    while (i < v.size() && std::isdigit(static_cast<unsigned char>(v[i]))) minor = minor * 10 + (v[i++] - '0');
    return {major, minor};
}

static int versionCode(const std::pair<int, int>& v) { return v.first * 100 + v.second * 10; }

static std::pair<int, int> runtimeGlVersion(const char* text) {
    const auto parsed = parseGlVersion(text);
    GLint major = 0;
    GLint minor = 0;
    while (glGetError() != GL_NO_ERROR) {}
    glGetIntegerv(GL_MAJOR_VERSION, &major);
    const GLenum majorError = glGetError();
    while (glGetError() != GL_NO_ERROR) {}
    glGetIntegerv(GL_MINOR_VERSION, &minor);
    const GLenum minorError = glGetError();
    if (majorError == GL_NO_ERROR && minorError == GL_NO_ERROR && major >= 3 && minor >= 0) return {major, minor};
    return parsed;
}


static void clearGlErrors() { while (glGetError() != GL_NO_ERROR) {} }

struct QueryDiagnosticNative { std::string name; std::string status; std::string detail; };
static thread_local std::vector<QueryDiagnosticNative>* activeDiagnostics = nullptr;

static std::string glErrorHex(GLenum error) { std::ostringstream o; o << "0x" << std::uppercase << std::hex << static_cast<unsigned int>(error); return o.str(); }
static void diagnostic(const char* name, GLenum error) { if (activeDiagnostics) activeDiagnostics->push_back({name, error == GL_NO_ERROR ? "Available" : "Unavailable", error == GL_NO_ERROR ? "" : std::string("GL error ") + glErrorHex(error)}); }


static void addLimit(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint v = 0;
    clearGlErrors();
    glGetIntegerv(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(std::to_string(v)) << '}';
}

static void addLimit2(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint v[2] = {0, 0};
    clearGlErrors();
    glGetIntegerv(e, v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(std::to_string(v[0]) + " × " + std::to_string(v[1])) << '}';
}

static void addFloatLimit(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLfloat v = 0.0f;
    clearGlErrors();
    glGetFloatv(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    std::ostringstream s;
    s << v;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(s.str()) << '}';
}

static void addFloatLimit2(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLfloat v[2] = {0.0f, 0.0f};
    clearGlErrors();
    glGetFloatv(e, v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    std::ostringstream s;
    s << v[0] << " … " << v[1];
    o << "{\"name\":" << q(name) << ",\"value\":" << q(s.str()) << '}';
}

static void addBooleanLimit(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLboolean v = GL_FALSE;
    clearGlErrors();
    glGetBooleanv(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(v == GL_TRUE ? "True" : "False") << '}';
}

static void addHexLimit(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint v = 0;
    clearGlErrors();
    glGetIntegerv(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(hexv(v)) << '}';
}

static void addLimit64(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint64 v = 0;
    clearGlErrors();
    glGetInteger64v(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(std::to_string(v)) << '}';
}

static void addIndexedLimit(std::ostringstream& o, bool& first, const char* name, GLenum e, GLuint index) {
    GLint v = 0;
    clearGlErrors();
    glGetIntegeri_v(e, index, &v);
    const GLenum error = glGetError();
    const std::string indexedName = std::string(name) + "[" + std::to_string(index) + "]";
    diagnostic(indexedName.c_str(), error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(indexedName) << ",\"value\":" << q(std::to_string(v)) << '}';
}

static std::vector<std::string> glExtensions(int glCode) {
    std::vector<std::string> out;
    if (glCode >= 300) {
        GLint n = 0;
        clearGlErrors();
        glGetIntegerv(GL_NUM_EXTENSIONS, &n);
        GLenum error = glGetError();
        if (error != GL_NO_ERROR || n < 0 || n >= 65536) {
            diagnostic("GL_EXTENSIONS", error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
            return out;
        }
        out.reserve(static_cast<size_t>(n));
        for (GLint i = 0; i < n; ++i) {
            clearGlErrors();
            const auto* extension = reinterpret_cast<const char*>(glGetStringi(GL_EXTENSIONS, static_cast<GLuint>(i)));
            error = glGetError();
            if (error != GL_NO_ERROR || extension == nullptr) {
                out.clear();
                diagnostic("GL_EXTENSIONS", error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
                return out;
            }
            if (*extension) out.emplace_back(extension);
        }
        diagnostic("GL_EXTENSIONS", GL_NO_ERROR);
    } else {
        clearGlErrors();
        const auto* extensionString = reinterpret_cast<const char*>(glGetString(GL_EXTENSIONS));
        const GLenum error = glGetError();
        if (error != GL_NO_ERROR || extensionString == nullptr) {
            diagnostic("GL_EXTENSIONS", error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
            return out;
        }
        out = splitExt(extensionString);
        diagnostic("GL_EXTENSIONS", GL_NO_ERROR);
    }
    return out;
}

static bool chooseConfig(EGLDisplay d, EGLint renderableBit, EGLConfig& cfg) {
    const EGLint preferred[] = {
        EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
        EGL_RENDERABLE_TYPE, renderableBit,
        EGL_RED_SIZE, 8,
        EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8,
        EGL_ALPHA_SIZE, 8,
        EGL_NONE
    };
    const EGLint minimal[] = {
        EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
        EGL_RENDERABLE_TYPE, renderableBit,
        EGL_NONE
    };
    EGLint count = 0;
    cfg = nullptr;
    if (eglChooseConfig(d, preferred, &cfg, 1, &count) == EGL_TRUE && count > 0 && cfg != nullptr) return true;
    count = 0;
    cfg = nullptr;
    return eglChooseConfig(d, minimal, &cfg, 1, &count) == EGL_TRUE && count > 0 && cfg != nullptr;
}

static EGLContext createBestContext(EGLDisplay d, EGLConfig& cfg, int eglCode, const std::vector<std::string>& displayExt) {
    eglBindAPI(EGL_OPENGL_ES_API);
    const bool canMinor = eglCode >= 150 || hasExt(displayExt, "EGL_KHR_create_context");
    if (canMinor && chooseConfig(d, EGL_OPENGL_ES3_BIT_KHR, cfg)) {
        const EGLint versions[][5] = {
            {EGL_CONTEXT_MAJOR_VERSION_KHR, 3, EGL_CONTEXT_MINOR_VERSION_KHR, 2, EGL_NONE},
            {EGL_CONTEXT_MAJOR_VERSION_KHR, 3, EGL_CONTEXT_MINOR_VERSION_KHR, 1, EGL_NONE},
            {EGL_CONTEXT_MAJOR_VERSION_KHR, 3, EGL_CONTEXT_MINOR_VERSION_KHR, 0, EGL_NONE}
        };
        for (const auto& attrs : versions) {
            EGLContext c = eglCreateContext(d, cfg, EGL_NO_CONTEXT, attrs);
            if (c != EGL_NO_CONTEXT) return c;
        }
    }
    if (chooseConfig(d, EGL_OPENGL_ES3_BIT_KHR, cfg)) {
        const EGLint attrs[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
        EGLContext c = eglCreateContext(d, cfg, EGL_NO_CONTEXT, attrs);
        if (c != EGL_NO_CONTEXT) return c;
    }
    if (chooseConfig(d, EGL_OPENGL_ES2_BIT, cfg)) {
        const EGLint es3Attrs[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
        EGLContext c = eglCreateContext(d, cfg, EGL_NO_CONTEXT, es3Attrs);
        if (c != EGL_NO_CONTEXT) return c;
        const EGLint es2Attrs[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
        return eglCreateContext(d, cfg, EGL_NO_CONTEXT, es2Attrs);
    }
    return EGL_NO_CONTEXT;
}

static EGLint cfgAttr(EGLDisplay d, EGLConfig c, EGLint attr, bool& ok) {
    EGLint v = 0;
    if (eglGetConfigAttrib(d, c, attr, &v) != EGL_TRUE) {
        ok = false;
        return 0;
    }
    ok = true;
    return v;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_efishell_openglesscope_OpenGLESProbeService_nativeCollect(JNIEnv* env, jobject) {
    EGLDisplay d = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (d == EGL_NO_DISPLAY) return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL display unavailable\"}");

    EGLint eglMajor = 0;
    EGLint eglMinor = 0;
    if (eglInitialize(d, &eglMajor, &eglMinor) != EGL_TRUE) return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"eglInitialize failed\"}");

    EGLConfig cfg = nullptr;
    const char* displayExtensionText = eglQueryString(d, EGL_EXTENSIONS);
    const auto displayExt = splitExt(displayExtensionText);
    const int eglCode = eglMajor * 100 + eglMinor * 10;
    EGLContext c = createBestContext(d, cfg, eglCode, displayExt);
    if (c == EGL_NO_CONTEXT || cfg == nullptr) {
        eglTerminate(d);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"OpenGL ES context creation failed\"}");
    }

    const EGLint pbAttrs[] = {EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE};
    EGLSurface s = eglCreatePbufferSurface(d, cfg, pbAttrs);
    if (s == EGL_NO_SURFACE) {
        eglDestroyContext(d, c);
        eglTerminate(d);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL pbuffer creation failed\"}");
    }
    if (eglMakeCurrent(d, s, s, c) != EGL_TRUE) {
        eglDestroySurface(d, s);
        eglDestroyContext(d, c);
        eglTerminate(d);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"eglMakeCurrent failed\"}");
    }

    const char* glVendor = reinterpret_cast<const char*>(glGetString(GL_VENDOR));
    const char* glRenderer = reinterpret_cast<const char*>(glGetString(GL_RENDERER));
    const char* glVersion = reinterpret_cast<const char*>(glGetString(GL_VERSION));
    const char* glslVersion = reinterpret_cast<const char*>(glGetString(GL_SHADING_LANGUAGE_VERSION));
    if (glVendor == nullptr || glRenderer == nullptr || glVersion == nullptr || glslVersion == nullptr) {
        eglMakeCurrent(d, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(d, s);
        eglDestroyContext(d, c);
        eglTerminate(d);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"Required OpenGL ES identity strings were unavailable\"}");
    }
    const auto parsed = runtimeGlVersion(glVersion);
    const int glCode = versionCode(parsed);
    std::vector<QueryDiagnosticNative> diagnostics;
    activeDiagnostics = &diagnostics;
    const auto glExt = glExtensions(glCode);
    const char* clientExtensionText = eglQueryString(EGL_NO_DISPLAY, EGL_EXTENSIONS);
    const auto clientExt = splitExt(clientExtensionText);
    const char* eglVendorText = eglQueryString(d, EGL_VENDOR);
    const char* eglVersionText = eglQueryString(d, EGL_VERSION);
    const char* eglClientApisText = eglQueryString(d, EGL_CLIENT_APIS);
    diagnostics.push_back({"EGL_EXTENSIONS", displayExtensionText ? "Available" : "Unavailable", displayExtensionText ? "" : "eglQueryString returned null"});
    diagnostics.push_back({"EGL_NO_DISPLAY/EGL_EXTENSIONS", clientExtensionText ? "Available" : "Unavailable", clientExtensionText ? "" : "eglQueryString returned null"});
    diagnostics.push_back({"EGL_VENDOR", eglVendorText ? "Available" : "Unavailable", eglVendorText ? "" : "eglQueryString returned null"});
    diagnostics.push_back({"EGL_VERSION", eglVersionText ? "Available" : "Unavailable", eglVersionText ? "" : "eglQueryString returned null"});
    diagnostics.push_back({"EGL_CLIENT_APIS", eglClientApisText ? "Available" : "Unavailable", eglClientApisText ? "" : "eglQueryString returned null"});

    std::ostringstream o;
    o << "{\"status\":\"available\",\"renderer\":" << q(glRenderer)
      << ",\"vendor\":" << q(glVendor)
      << ",\"glVersion\":" << q(glVersion)
      << ",\"glMajor\":" << parsed.first
      << ",\"glMinor\":" << parsed.second
      << ",\"glslVersion\":" << q(glslVersion)
      << ",\"egl\":{\"vendor\":" << q(eglVendorText ? eglVendorText : "Unavailable")
      << ",\"version\":" << q(eglVersionText ? eglVersionText : "Unavailable")
      << ",\"initializedVersion\":" << q(std::to_string(eglMajor) + "." + std::to_string(eglMinor))
      << ",\"clientApis\":" << q(eglClientApisText ? eglClientApisText : "Unavailable")
      << ",\"extensions\":";
    appendStringArray(o, displayExt);
    o << ",\"clientExtensions\":";
    appendStringArray(o, clientExt);
    o << "},\"extensions\":";
    appendStringArray(o, glExt);

    o << ",\"limits\":[";
    bool first = true;
    addLimit(o, first, "GL_MAX_TEXTURE_SIZE", GL_MAX_TEXTURE_SIZE);
    addLimit(o, first, "GL_NUM_COMPRESSED_TEXTURE_FORMATS", GL_NUM_COMPRESSED_TEXTURE_FORMATS);
    addLimit(o, first, "GL_NUM_SHADER_BINARY_FORMATS", GL_NUM_SHADER_BINARY_FORMATS);
    addLimit(o, first, "GL_MAX_CUBE_MAP_TEXTURE_SIZE", GL_MAX_CUBE_MAP_TEXTURE_SIZE);
    addLimit(o, first, "GL_MAX_RENDERBUFFER_SIZE", GL_MAX_RENDERBUFFER_SIZE);
    addLimit2(o, first, "GL_MAX_VIEWPORT_DIMS", GL_MAX_VIEWPORT_DIMS);
    addFloatLimit2(o, first, "GL_ALIASED_LINE_WIDTH_RANGE", GL_ALIASED_LINE_WIDTH_RANGE);
    addFloatLimit2(o, first, "GL_ALIASED_POINT_SIZE_RANGE", GL_ALIASED_POINT_SIZE_RANGE);
    addLimit(o, first, "GL_SUBPIXEL_BITS", GL_SUBPIXEL_BITS);
    addBooleanLimit(o, first, "GL_SHADER_COMPILER", GL_SHADER_COMPILER);
    addHexLimit(o, first, "GL_IMPLEMENTATION_COLOR_READ_FORMAT", GL_IMPLEMENTATION_COLOR_READ_FORMAT);
    addHexLimit(o, first, "GL_IMPLEMENTATION_COLOR_READ_TYPE", GL_IMPLEMENTATION_COLOR_READ_TYPE);
    if (hasExt(glExt, "GL_EXT_texture_filter_anisotropic")) addFloatLimit(o, first, "GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT", 0x84FF);
    if (hasExt(glExt, "GL_EXT_blend_func_extended")) addLimit(o, first, "GL_MAX_DUAL_SOURCE_DRAW_BUFFERS_EXT", 0x88FC);
    if (hasExt(glExt, "GL_OVR_multiview") || hasExt(glExt, "GL_OVR_multiview2")) addLimit(o, first, "GL_MAX_VIEWS_OVR", 0x9631);
    if (hasExt(glExt, "GL_EXT_multiview_draw_buffers")) addLimit(o, first, "GL_MAX_MULTIVIEW_BUFFERS_EXT", 0x90F2);
    if (hasExt(glExt, "GL_EXT_texture_buffer")) {
        addLimit(o, first, "GL_MAX_TEXTURE_BUFFER_SIZE_EXT", 0x8C2B);
        addLimit(o, first, "GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT_EXT", 0x919F);
    }
    if (hasExt(glExt, "GL_EXT_clip_cull_distance")) {
        addLimit(o, first, "GL_MAX_CLIP_DISTANCES_EXT", 0x0D32);
        addLimit(o, first, "GL_MAX_CULL_DISTANCES_EXT", 0x82F9);
        addLimit(o, first, "GL_MAX_COMBINED_CLIP_AND_CULL_DISTANCES_EXT", 0x82FA);
    }
    if (glCode < 300 && hasExt(glExt, "GL_EXT_draw_buffers")) {
        addLimit(o, first, "GL_MAX_DRAW_BUFFERS_EXT", 0x8824);
        addLimit(o, first, "GL_MAX_COLOR_ATTACHMENTS_EXT", 0x8CDF);
    }
    if (glCode < 300 && hasExt(glExt, "GL_NV_draw_buffers")) addLimit(o, first, "GL_MAX_DRAW_BUFFERS_NV", 0x8824);
    if (glCode < 300 && hasExt(glExt, "GL_EXT_multisampled_render_to_texture")) addLimit(o, first, "GL_MAX_SAMPLES_EXT", 0x8D57);
    if (glCode < 300 && hasExt(glExt, "GL_NV_framebuffer_multisample")) addLimit(o, first, "GL_MAX_SAMPLES_NV", 0x8D57);
    if (glCode < 300 && hasExt(glExt, "GL_IMG_multisampled_render_to_texture")) addLimit(o, first, "GL_MAX_SAMPLES_IMG", 0x9135);
    if (glCode < 300 && hasExt(glExt, "GL_OES_get_program_binary")) addLimit(o, first, "GL_NUM_PROGRAM_BINARY_FORMATS", 0x87FE);
    addLimit(o, first, "GL_MAX_VERTEX_ATTRIBS", GL_MAX_VERTEX_ATTRIBS);
    addLimit(o, first, "GL_MAX_VERTEX_UNIFORM_VECTORS", GL_MAX_VERTEX_UNIFORM_VECTORS);
    addLimit(o, first, "GL_MAX_FRAGMENT_UNIFORM_VECTORS", GL_MAX_FRAGMENT_UNIFORM_VECTORS);
    addLimit(o, first, "GL_MAX_VARYING_VECTORS", GL_MAX_VARYING_VECTORS);
    addLimit(o, first, "GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS", GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
    addLimit(o, first, "GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS", GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS);
    addLimit(o, first, "GL_MAX_TEXTURE_IMAGE_UNITS", GL_MAX_TEXTURE_IMAGE_UNITS);

    if (glCode >= 300) {
        addLimit(o, first, "GL_NUM_EXTENSIONS", GL_NUM_EXTENSIONS);
        addLimit(o, first, "GL_CONTEXT_FLAGS", GL_CONTEXT_FLAGS);
        addLimit(o, first, "GL_MAX_3D_TEXTURE_SIZE", GL_MAX_3D_TEXTURE_SIZE);
        addLimit(o, first, "GL_NUM_PROGRAM_BINARY_FORMATS", GL_NUM_PROGRAM_BINARY_FORMATS);
        addLimit64(o, first, "GL_MAX_ELEMENT_INDEX", GL_MAX_ELEMENT_INDEX);
        addLimit64(o, first, "GL_MAX_SERVER_WAIT_TIMEOUT", GL_MAX_SERVER_WAIT_TIMEOUT);
        addLimit64(o, first, "GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS", GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS);
        addLimit64(o, first, "GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS", GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_ARRAY_TEXTURE_LAYERS", GL_MAX_ARRAY_TEXTURE_LAYERS);
        addLimit(o, first, "GL_MAX_COLOR_ATTACHMENTS", GL_MAX_COLOR_ATTACHMENTS);
        addLimit(o, first, "GL_MAX_COMBINED_UNIFORM_BLOCKS", GL_MAX_COMBINED_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_DRAW_BUFFERS", GL_MAX_DRAW_BUFFERS);
        addLimit(o, first, "GL_MAX_ELEMENTS_INDICES", GL_MAX_ELEMENTS_INDICES);
        addLimit(o, first, "GL_MAX_ELEMENTS_VERTICES", GL_MAX_ELEMENTS_VERTICES);
        addLimit(o, first, "GL_MAX_FRAGMENT_INPUT_COMPONENTS", GL_MAX_FRAGMENT_INPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_FRAGMENT_UNIFORM_BLOCKS", GL_MAX_FRAGMENT_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_FRAGMENT_UNIFORM_COMPONENTS", GL_MAX_FRAGMENT_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_PROGRAM_TEXEL_OFFSET", GL_MAX_PROGRAM_TEXEL_OFFSET);
        addLimit(o, first, "GL_MIN_PROGRAM_TEXEL_OFFSET", GL_MIN_PROGRAM_TEXEL_OFFSET);
        addLimit(o, first, "GL_MAX_SAMPLES", GL_MAX_SAMPLES);
        addFloatLimit(o, first, "GL_MAX_TEXTURE_LOD_BIAS", GL_MAX_TEXTURE_LOD_BIAS);
        addLimit(o, first, "GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS", GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS);
        addLimit(o, first, "GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS", GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS);
        addLimit(o, first, "GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS", GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS);
        addLimit64(o, first, "GL_MAX_UNIFORM_BLOCK_SIZE", GL_MAX_UNIFORM_BLOCK_SIZE);
        addLimit(o, first, "GL_MAX_UNIFORM_BUFFER_BINDINGS", GL_MAX_UNIFORM_BUFFER_BINDINGS);
        addLimit(o, first, "GL_MAX_VARYING_COMPONENTS", GL_MAX_VARYING_COMPONENTS);
        addLimit(o, first, "GL_MAX_VERTEX_OUTPUT_COMPONENTS", GL_MAX_VERTEX_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_VERTEX_UNIFORM_BLOCKS", GL_MAX_VERTEX_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_VERTEX_UNIFORM_COMPONENTS", GL_MAX_VERTEX_UNIFORM_COMPONENTS);
    }

    if (glCode >= 310) {
        addLimit(o, first, "GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS", GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS);
        addLimit(o, first, "GL_MAX_ATOMIC_COUNTER_BUFFER_SIZE", GL_MAX_ATOMIC_COUNTER_BUFFER_SIZE);
        addLimit(o, first, "GL_MAX_COLOR_TEXTURE_SAMPLES", GL_MAX_COLOR_TEXTURE_SAMPLES);
        addLimit(o, first, "GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES", GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES);
        addLimit(o, first, "GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS", GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS);
        addLimit(o, first, "GL_MAX_DEPTH_TEXTURE_SAMPLES", GL_MAX_DEPTH_TEXTURE_SAMPLES);
        addLimit(o, first, "GL_MAX_FRAGMENT_ATOMIC_COUNTERS", GL_MAX_FRAGMENT_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS", GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_FRAGMENT_IMAGE_UNIFORMS", GL_MAX_FRAGMENT_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS", GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_INTEGER_SAMPLES", GL_MAX_INTEGER_SAMPLES);
        addLimit(o, first, "GL_MAX_VERTEX_ATOMIC_COUNTERS", GL_MAX_VERTEX_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS", GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_VERTEX_IMAGE_UNIFORMS", GL_MAX_VERTEX_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS", GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_COMBINED_ATOMIC_COUNTERS", GL_MAX_COMBINED_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS", GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS", GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_COMBINED_IMAGE_UNIFORMS", GL_MAX_COMBINED_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS", GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_COMPUTE_ATOMIC_COUNTERS", GL_MAX_COMPUTE_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS", GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_COMPUTE_IMAGE_UNIFORMS", GL_MAX_COMPUTE_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS", GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_COMPUTE_SHARED_MEMORY_SIZE", GL_MAX_COMPUTE_SHARED_MEMORY_SIZE);
        addLimit(o, first, "GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS", GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS);
        addLimit(o, first, "GL_MAX_COMPUTE_UNIFORM_BLOCKS", GL_MAX_COMPUTE_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_COMPUTE_UNIFORM_COMPONENTS", GL_MAX_COMPUTE_UNIFORM_COMPONENTS);
        for (GLuint i = 0; i < 3; ++i) addIndexedLimit(o, first, "GL_MAX_COMPUTE_WORK_GROUP_COUNT", GL_MAX_COMPUTE_WORK_GROUP_COUNT, i);
        for (GLuint i = 0; i < 3; ++i) addIndexedLimit(o, first, "GL_MAX_COMPUTE_WORK_GROUP_SIZE", GL_MAX_COMPUTE_WORK_GROUP_SIZE, i);
        addLimit(o, first, "GL_MAX_FRAMEBUFFER_HEIGHT", GL_MAX_FRAMEBUFFER_HEIGHT);
        addLimit(o, first, "GL_MAX_FRAMEBUFFER_SAMPLES", GL_MAX_FRAMEBUFFER_SAMPLES);
        addLimit(o, first, "GL_MAX_FRAMEBUFFER_WIDTH", GL_MAX_FRAMEBUFFER_WIDTH);
        addLimit(o, first, "GL_MAX_IMAGE_UNITS", GL_MAX_IMAGE_UNITS);
        addLimit(o, first, "GL_MAX_PROGRAM_TEXTURE_GATHER_OFFSET", GL_MAX_PROGRAM_TEXTURE_GATHER_OFFSET);
        addLimit(o, first, "GL_MIN_PROGRAM_TEXTURE_GATHER_OFFSET", GL_MIN_PROGRAM_TEXTURE_GATHER_OFFSET);
        addLimit(o, first, "GL_MAX_SAMPLE_MASK_WORDS", GL_MAX_SAMPLE_MASK_WORDS);
        addLimit64(o, first, "GL_MAX_SHADER_STORAGE_BLOCK_SIZE", GL_MAX_SHADER_STORAGE_BLOCK_SIZE);
        addLimit(o, first, "GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS", GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS);
        addLimit(o, first, "GL_MAX_UNIFORM_LOCATIONS", GL_MAX_UNIFORM_LOCATIONS);
        addLimit(o, first, "GL_MAX_VERTEX_ATTRIB_BINDINGS", GL_MAX_VERTEX_ATTRIB_BINDINGS);
        addLimit(o, first, "GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET", GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET);
        addLimit(o, first, "GL_MAX_VERTEX_ATTRIB_STRIDE", GL_MAX_VERTEX_ATTRIB_STRIDE);
    }

    if (glCode >= 320) {
        addFloatLimit(o, first, "GL_MIN_SAMPLE_SHADING_VALUE", GL_MIN_SAMPLE_SHADING_VALUE);
        addLimit64(o, first, "GL_MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS", GL_MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS);
        addLimit64(o, first, "GL_MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS", GL_MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS);
        addLimit64(o, first, "GL_MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS", GL_MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_DEBUG_GROUP_STACK_DEPTH", GL_MAX_DEBUG_GROUP_STACK_DEPTH);
        addLimit(o, first, "GL_MAX_DEBUG_LOGGED_MESSAGES", GL_MAX_DEBUG_LOGGED_MESSAGES);
        addLimit(o, first, "GL_MAX_DEBUG_MESSAGE_LENGTH", GL_MAX_DEBUG_MESSAGE_LENGTH);
        addFloatLimit(o, first, "GL_MIN_FRAGMENT_INTERPOLATION_OFFSET", GL_MIN_FRAGMENT_INTERPOLATION_OFFSET);
        addFloatLimit(o, first, "GL_MAX_FRAGMENT_INTERPOLATION_OFFSET", GL_MAX_FRAGMENT_INTERPOLATION_OFFSET);
        addLimit(o, first, "GL_MAX_FRAMEBUFFER_LAYERS", GL_MAX_FRAMEBUFFER_LAYERS);
        addLimit(o, first, "GL_MAX_LABEL_LENGTH", GL_MAX_LABEL_LENGTH);
        addLimit(o, first, "GL_MAX_PATCH_VERTICES", GL_MAX_PATCH_VERTICES);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS", GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS", GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS", GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_INPUT_COMPONENTS", GL_MAX_TESS_CONTROL_INPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS", GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS", GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS", GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS", GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS", GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS", GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS", GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS", GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS", GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS", GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS", GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS", GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS", GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS", GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS", GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_GEOMETRY_ATOMIC_COUNTERS", GL_MAX_GEOMETRY_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS", GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_GEOMETRY_IMAGE_UNIFORMS", GL_MAX_GEOMETRY_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_GEOMETRY_INPUT_COMPONENTS", GL_MAX_GEOMETRY_INPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_GEOMETRY_OUTPUT_COMPONENTS", GL_MAX_GEOMETRY_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_GEOMETRY_OUTPUT_VERTICES", GL_MAX_GEOMETRY_OUTPUT_VERTICES);
        addLimit(o, first, "GL_MAX_GEOMETRY_SHADER_INVOCATIONS", GL_MAX_GEOMETRY_SHADER_INVOCATIONS);
        addLimit(o, first, "GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS", GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS", GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS);
        addLimit(o, first, "GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS", GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_GEOMETRY_UNIFORM_BLOCKS", GL_MAX_GEOMETRY_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_GEOMETRY_UNIFORM_COMPONENTS", GL_MAX_GEOMETRY_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_GEN_LEVEL", GL_MAX_TESS_GEN_LEVEL);
        addLimit(o, first, "GL_MAX_TESS_PATCH_COMPONENTS", GL_MAX_TESS_PATCH_COMPONENTS);
        addLimit(o, first, "GL_MAX_TEXTURE_BUFFER_SIZE", GL_MAX_TEXTURE_BUFFER_SIZE);
        addFloatLimit2(o, first, "GL_MULTISAMPLE_LINE_WIDTH_RANGE", GL_MULTISAMPLE_LINE_WIDTH_RANGE);
        addFloatLimit(o, first, "GL_MULTISAMPLE_LINE_WIDTH_GRANULARITY", GL_MULTISAMPLE_LINE_WIDTH_GRANULARITY);
        addLimit(o, first, "GL_FRAGMENT_INTERPOLATION_OFFSET_BITS", GL_FRAGMENT_INTERPOLATION_OFFSET_BITS);
        addHexLimit(o, first, "GL_LAYER_PROVOKING_VERTEX", GL_LAYER_PROVOKING_VERTEX);
        addBooleanLimit(o, first, "GL_PRIMITIVE_RESTART_FOR_PATCHES_SUPPORTED", GL_PRIMITIVE_RESTART_FOR_PATCHES_SUPPORTED);
        addLimit(o, first, "GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT", GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT);
    }
    o << ']';

    auto appendEnumArray = [&](const char* key, GLenum countEnum, GLenum valuesEnum) {
        GLint count = 0;
        clearGlErrors();
        glGetIntegerv(countEnum, &count);
        const GLenum countError = glGetError();
        std::vector<GLint> values;
        GLenum valueError = GL_NO_ERROR;
        if (countError == GL_NO_ERROR && count >= 0 && count < 65536) {
            values.resize(static_cast<size_t>(count));
            if (count > 0) {
                clearGlErrors();
                glGetIntegerv(valuesEnum, values.data());
                valueError = glGetError();
                if (valueError != GL_NO_ERROR) values.clear();
            }
        } else if (countError == GL_NO_ERROR) {
            valueError = GL_INVALID_VALUE;
        }
        diagnostic(key, countError != GL_NO_ERROR ? countError : valueError);
        o << ",\"" << key << "\":[";
        for (size_t i = 0; i < values.size(); ++i) {
            if (i) o << ',';
            o << q(enumDisplay(values[i], key));
        }
        o << ']';
    };

    appendEnumArray("shaderBinaryFormats", GL_NUM_SHADER_BINARY_FORMATS, GL_SHADER_BINARY_FORMATS);
    if (glCode >= 300 || hasExt(glExt, "GL_OES_get_program_binary")) {
        appendEnumArray("programBinaryFormats", GL_NUM_PROGRAM_BINARY_FORMATS, GL_PROGRAM_BINARY_FORMATS);
    } else {
        diagnostics.push_back({"programBinaryFormats", "Not applicable", "Requires OpenGL ES 3.0 or GL_OES_get_program_binary"});
        o << ",\"programBinaryFormats\":[]";
    }

    GLint nfmt = 0;
    clearGlErrors();
    glGetIntegerv(GL_NUM_COMPRESSED_TEXTURE_FORMATS, &nfmt);
    const GLenum compressedCountError = glGetError();
    std::vector<GLint> fmts;
    GLenum compressedValuesError = GL_NO_ERROR;
    if (compressedCountError == GL_NO_ERROR && nfmt >= 0 && nfmt < 65536) {
        fmts.resize(static_cast<size_t>(nfmt));
        if (nfmt > 0) {
            clearGlErrors();
            glGetIntegerv(GL_COMPRESSED_TEXTURE_FORMATS, fmts.data());
            compressedValuesError = glGetError();
            if (compressedValuesError != GL_NO_ERROR) fmts.clear();
        }
    } else if (compressedCountError == GL_NO_ERROR) {
        compressedValuesError = GL_INVALID_VALUE;
    }
    diagnostic("compressedFormats", compressedCountError != GL_NO_ERROR ? compressedCountError : compressedValuesError);
    o << ",\"compressedFormats\":[";
    for (size_t i = 0; i < fmts.size(); ++i) {
        if (i) o << ',';
        o << q(enumDisplay(fmts[i], "compressedFormats"));
    }
    o << ']';

    const GLenum shaders[] = {GL_VERTEX_SHADER, GL_FRAGMENT_SHADER};
    const char* shaderNames[] = {"GL_VERTEX_SHADER", "GL_FRAGMENT_SHADER"};
    const GLenum precisions[] = {GL_LOW_FLOAT, GL_MEDIUM_FLOAT, GL_HIGH_FLOAT, GL_LOW_INT, GL_MEDIUM_INT, GL_HIGH_INT};
    const char* precisionNames[] = {"GL_LOW_FLOAT", "GL_MEDIUM_FLOAT", "GL_HIGH_FLOAT", "GL_LOW_INT", "GL_MEDIUM_INT", "GL_HIGH_INT"};
    o << ",\"precision\":[";
    bool pf = true;
    for (int si = 0; si < 2; ++si) {
        for (int pi = 0; pi < 6; ++pi) {
            GLint range[2] = {0, 0};
            GLint precision = 0;
            clearGlErrors();
            glGetShaderPrecisionFormat(shaders[si], precisions[pi], range, &precision);
            const GLenum precisionError = glGetError();
            const std::string precisionQuery = std::string(shaderNames[si]) + "/" + precisionNames[pi];
            diagnostic(precisionQuery.c_str(), precisionError);
            if (precisionError != GL_NO_ERROR) continue;
            if (!pf) o << ',';
            pf = false;
            o << "{\"shader\":" << q(shaderNames[si]) << ",\"type\":" << q(precisionNames[pi])
              << ",\"rangeMin\":" << range[0] << ",\"rangeMax\":" << range[1] << ",\"precision\":" << precision << '}';
        }
    }
    o << ']';

    o << ",\"diagnostics\":[";
    for (size_t i = 0; i < diagnostics.size(); ++i) {
        if (i) o << ',';
        o << "{\"name\":" << q(diagnostics[i].name) << ",\"status\":" << q(diagnostics[i].status) << ",\"detail\":" << q(diagnostics[i].detail) << '}';
    }
    o << ']';
    activeDiagnostics = nullptr;

    EGLint totalConfigs = 0;
    if (eglGetConfigs(d, nullptr, 0, &totalConfigs) != EGL_TRUE || totalConfigs <= 0 || totalConfigs > 65536) {
        eglMakeCurrent(d, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(d, s);
        eglDestroyContext(d, c);
        eglTerminate(d);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL configuration enumeration failed\"}");
    }
    std::vector<EGLConfig> configs(static_cast<size_t>(totalConfigs));
    if (eglGetConfigs(d, configs.data(), totalConfigs, &totalConfigs) != EGL_TRUE || totalConfigs <= 0) {
        eglMakeCurrent(d, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroySurface(d, s);
        eglDestroyContext(d, c);
        eglTerminate(d);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL configuration data could not be read\"}");
    }
    o << ",\"eglConfigs\":[";
    bool configFirst = true;
    for (EGLint i = 0; i < totalConfigs; ++i) {
        const EGLConfig ec = configs[static_cast<size_t>(i)];
        bool ok = false;
        const EGLint id = cfgAttr(d, ec, EGL_CONFIG_ID, ok);
        if (!ok) {
            eglMakeCurrent(d, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
            eglDestroySurface(d, s);
            eglDestroyContext(d, c);
            eglTerminate(d);
            return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"An EGL configuration could not be identified\"}");
        }
        auto required = [&](EGLint attr) { bool good = false; const EGLint v = cfgAttr(d, ec, attr, good); return std::pair<EGLint, bool>{v, good}; };
        const auto red = required(EGL_RED_SIZE);
        const auto green = required(EGL_GREEN_SIZE);
        const auto blue = required(EGL_BLUE_SIZE);
        const auto alpha = required(EGL_ALPHA_SIZE);
        const auto depth = required(EGL_DEPTH_SIZE);
        const auto stencil = required(EGL_STENCIL_SIZE);
        const auto sampleBuffers = required(EGL_SAMPLE_BUFFERS);
        const auto samples = required(EGL_SAMPLES);
        const auto surfaceType = required(EGL_SURFACE_TYPE);
        const auto renderableType = required(EGL_RENDERABLE_TYPE);
        const auto conformant = required(EGL_CONFORMANT);
        const auto caveat = required(EGL_CONFIG_CAVEAT);
        const auto colorBufferType = required(EGL_COLOR_BUFFER_TYPE);
        const auto level = required(EGL_LEVEL);
        const auto nativeRenderable = required(EGL_NATIVE_RENDERABLE);
        const auto nativeVisualId = required(EGL_NATIVE_VISUAL_ID);
        const auto minSwap = required(EGL_MIN_SWAP_INTERVAL);
        const auto maxSwap = required(EGL_MAX_SWAP_INTERVAL);
        const auto bufferSize = required(EGL_BUFFER_SIZE);
        const auto luminanceSize = required(EGL_LUMINANCE_SIZE);
        const auto alphaMaskSize = required(EGL_ALPHA_MASK_SIZE);
        const auto bindRgb = required(EGL_BIND_TO_TEXTURE_RGB);
        const auto bindRgba = required(EGL_BIND_TO_TEXTURE_RGBA);
        const auto maxPbufferWidth = required(EGL_MAX_PBUFFER_WIDTH);
        const auto maxPbufferHeight = required(EGL_MAX_PBUFFER_HEIGHT);
        const auto maxPbufferPixels = required(EGL_MAX_PBUFFER_PIXELS);
        const auto nativeVisualType = required(EGL_NATIVE_VISUAL_TYPE);
        const auto transparentType = required(EGL_TRANSPARENT_TYPE);
        const auto transparentRed = required(EGL_TRANSPARENT_RED_VALUE);
        const auto transparentGreen = required(EGL_TRANSPARENT_GREEN_VALUE);
        const auto transparentBlue = required(EGL_TRANSPARENT_BLUE_VALUE);
        if (!configFirst) o << ',';
        configFirst = false;
        o << "{\"id\":" << id
          << ",\"red\":" << (red.second ? std::to_string(red.first) : "null")
          << ",\"green\":" << (green.second ? std::to_string(green.first) : "null")
          << ",\"blue\":" << (blue.second ? std::to_string(blue.first) : "null")
          << ",\"alpha\":" << (alpha.second ? std::to_string(alpha.first) : "null")
          << ",\"depth\":" << (depth.second ? std::to_string(depth.first) : "null")
          << ",\"stencil\":" << (stencil.second ? std::to_string(stencil.first) : "null")
          << ",\"sampleBuffers\":" << (sampleBuffers.second ? std::to_string(sampleBuffers.first) : "null")
          << ",\"samples\":" << (samples.second ? std::to_string(samples.first) : "null")
          << ",\"surfaceType\":" << (surfaceType.second ? q(hexv(surfaceType.first)) : "null")
          << ",\"renderableType\":" << (renderableType.second ? q(hexv(renderableType.first)) : "null")
          << ",\"conformant\":" << (conformant.second ? q(hexv(conformant.first)) : "null")
          << ",\"configCaveat\":" << (caveat.second ? q(hexv(caveat.first)) : "null")
          << ",\"colorBufferType\":" << (colorBufferType.second ? q(hexv(colorBufferType.first)) : "null")
          << ",\"level\":" << (level.second ? std::to_string(level.first) : "null")
          << ",\"nativeRenderable\":" << (nativeRenderable.second ? std::to_string(nativeRenderable.first) : "null")
          << ",\"nativeVisualId\":" << (nativeVisualId.second ? std::to_string(nativeVisualId.first) : "null")
          << ",\"minSwapInterval\":" << (minSwap.second ? std::to_string(minSwap.first) : "null")
          << ",\"maxSwapInterval\":" << (maxSwap.second ? std::to_string(maxSwap.first) : "null")
          << ",\"bufferSize\":" << (bufferSize.second ? std::to_string(bufferSize.first) : "null")
          << ",\"luminanceSize\":" << (luminanceSize.second ? std::to_string(luminanceSize.first) : "null")
          << ",\"alphaMaskSize\":" << (alphaMaskSize.second ? std::to_string(alphaMaskSize.first) : "null")
          << ",\"bindToTextureRgb\":" << (bindRgb.second ? std::to_string(bindRgb.first) : "null")
          << ",\"bindToTextureRgba\":" << (bindRgba.second ? std::to_string(bindRgba.first) : "null")
          << ",\"maxPbufferWidth\":" << (maxPbufferWidth.second ? std::to_string(maxPbufferWidth.first) : "null")
          << ",\"maxPbufferHeight\":" << (maxPbufferHeight.second ? std::to_string(maxPbufferHeight.first) : "null")
          << ",\"maxPbufferPixels\":" << (maxPbufferPixels.second ? std::to_string(maxPbufferPixels.first) : "null")
          << ",\"nativeVisualType\":" << (nativeVisualType.second ? std::to_string(nativeVisualType.first) : "null")
          << ",\"transparentType\":" << (transparentType.second ? q(hexv(transparentType.first)) : "null")
          << ",\"transparentRed\":" << (transparentRed.second ? std::to_string(transparentRed.first) : "null")
          << ",\"transparentGreen\":" << (transparentGreen.second ? std::to_string(transparentGreen.first) : "null")
          << ",\"transparentBlue\":" << (transparentBlue.second ? std::to_string(transparentBlue.first) : "null")
          << '}';
    }
    o << "]}";

    eglMakeCurrent(d, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    eglDestroySurface(d, s);
    eglDestroyContext(d, c);
    eglTerminate(d);
    const std::string result = o.str();
    return env->NewStringUTF(result.c_str());
}
