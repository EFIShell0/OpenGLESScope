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

static constexpr GLint kMaxGlEnumerationCount = 16384;
static constexpr EGLint kMaxEglConfigCount = 4096;
static constexpr GLint kMaxProgramBinaryBytes = 8 * 1024 * 1024;
static constexpr size_t kMaxRuntimeStringBytes = 1024 * 1024;
static constexpr size_t kMaxExtensionTokenBytes = 4096;
static constexpr GLint kMaxInfoLogBytes = 1024 * 1024;
static constexpr EGLint EGL_RECORDABLE_ANDROID_VALUE = 0x3142;
static constexpr EGLint EGL_FRAMEBUFFER_TARGET_ANDROID_VALUE = 0x3147;
static constexpr EGLint EGL_COLOR_COMPONENT_TYPE_EXT_VALUE = 0x3339;
static constexpr EGLint EGL_COLOR_COMPONENT_TYPE_FIXED_EXT_VALUE = 0x333A;
static constexpr EGLint EGL_COLOR_COMPONENT_TYPE_FLOAT_EXT_VALUE = 0x333B;

static std::string eglErrorName(EGLint error) {
    switch (error) {
        case EGL_SUCCESS: return "EGL_SUCCESS";
        case EGL_NOT_INITIALIZED: return "EGL_NOT_INITIALIZED";
        case EGL_BAD_ACCESS: return "EGL_BAD_ACCESS";
        case EGL_BAD_ALLOC: return "EGL_BAD_ALLOC";
        case EGL_BAD_ATTRIBUTE: return "EGL_BAD_ATTRIBUTE";
        case EGL_BAD_CONTEXT: return "EGL_BAD_CONTEXT";
        case EGL_BAD_CONFIG: return "EGL_BAD_CONFIG";
        case EGL_BAD_CURRENT_SURFACE: return "EGL_BAD_CURRENT_SURFACE";
        case EGL_BAD_DISPLAY: return "EGL_BAD_DISPLAY";
        case EGL_BAD_MATCH: return "EGL_BAD_MATCH";
        case EGL_BAD_NATIVE_PIXMAP: return "EGL_BAD_NATIVE_PIXMAP";
        case EGL_BAD_NATIVE_WINDOW: return "EGL_BAD_NATIVE_WINDOW";
        case EGL_BAD_PARAMETER: return "EGL_BAD_PARAMETER";
        case EGL_BAD_SURFACE: return "EGL_BAD_SURFACE";
        default: return "EGL_ERROR";
    }
}

static std::string eglErrorDisplay(EGLint error) { return eglErrorName(error) + " (" + hexv(error) + ")"; }

static void releaseEgl(EGLDisplay d, EGLSurface surface, EGLContext context) {
    if (d != EGL_NO_DISPLAY) {
        eglMakeCurrent(d, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (surface != EGL_NO_SURFACE) eglDestroySurface(d, surface);
        if (context != EGL_NO_CONTEXT) eglDestroyContext(d, context);
        eglTerminate(d);
    }
    eglReleaseThread();
}

struct EglAttrResult { EGLint value; bool available; EGLint error; };

static EglAttrResult queryConfigAttr(EGLDisplay d, EGLConfig c, EGLint attr) {
    eglGetError();
    EGLint value = 0;
    if (eglGetConfigAttrib(d, c, attr, &value) == EGL_TRUE) return {value, true, EGL_SUCCESS};
    return {0, false, eglGetError()};
}

static EglAttrResult queryContextAttr(EGLDisplay d, EGLContext c, EGLint attr) {
    eglGetError();
    EGLint value = 0;
    if (eglQueryContext(d, c, attr, &value) == EGL_TRUE) return {value, true, EGL_SUCCESS};
    return {0, false, eglGetError()};
}

static EglAttrResult querySurfaceAttr(EGLDisplay d, EGLSurface surface, EGLint attr) {
    eglGetError();
    EGLint value = 0;
    if (eglQuerySurface(d, surface, attr, &value) == EGL_TRUE) return {value, true, EGL_SUCCESS};
    return {0, false, eglGetError()};
}

static std::string eglApiDisplay(EGLenum api) {
    if (api == EGL_OPENGL_ES_API) return "EGL_OPENGL_ES_API (0x30A0)";
    if (api == EGL_OPENGL_API) return "EGL_OPENGL_API (0x30A2)";
    if (api == EGL_OPENVG_API) return "EGL_OPENVG_API (0x30A1)";
    return hexv(static_cast<EGLint>(api));
}

static std::string eglEnumDisplay(EGLint value) {
    if (value == EGL_BACK_BUFFER) return "EGL_BACK_BUFFER (0x3084)";
    if (value == EGL_SINGLE_BUFFER) return "EGL_SINGLE_BUFFER (0x3085)";
    if (value == EGL_BUFFER_PRESERVED) return "EGL_BUFFER_PRESERVED (0x3094)";
    if (value == EGL_BUFFER_DESTROYED) return "EGL_BUFFER_DESTROYED (0x3095)";
    if (value == EGL_NO_TEXTURE) return "EGL_NO_TEXTURE (0x305C)";
    if (value == EGL_TEXTURE_RGB) return "EGL_TEXTURE_RGB (0x305D)";
    if (value == EGL_TEXTURE_RGBA) return "EGL_TEXTURE_RGBA (0x305E)";
    if (value == EGL_TEXTURE_2D) return "EGL_TEXTURE_2D (0x305F)";
    if (value == EGL_MULTISAMPLE_RESOLVE_DEFAULT) return "EGL_MULTISAMPLE_RESOLVE_DEFAULT (0x309A)";
    if (value == EGL_MULTISAMPLE_RESOLVE_BOX) return "EGL_MULTISAMPLE_RESOLVE_BOX (0x309B)";
    if (value == EGL_OPENGL_ES_API) return "EGL_OPENGL_ES_API (0x30A0)";
    if (value == EGL_OPENGL_API) return "EGL_OPENGL_API (0x30A2)";
    if (value == EGL_OPENVG_API) return "EGL_OPENVG_API (0x30A1)";
    if (value == EGL_COLOR_COMPONENT_TYPE_FIXED_EXT_VALUE) return "EGL_COLOR_COMPONENT_TYPE_FIXED_EXT (0x333A)";
    if (value == EGL_COLOR_COMPONENT_TYPE_FLOAT_EXT_VALUE) return "EGL_COLOR_COMPONENT_TYPE_FLOAT_EXT (0x333B)";
    return hexv(value);
}


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
        if (value == 0x8C4C) return "GL_COMPRESSED_SRGB_S3TC_DXT1_EXT (" + hexv(value) + ")";
        if (value == 0x8C4D) return "GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT (" + hexv(value) + ")";
        if (value == 0x8C4E) return "GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT (" + hexv(value) + ")";
        if (value == 0x8C4F) return "GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT (" + hexv(value) + ")";
        if (value == 0x8DBB) return "GL_COMPRESSED_RED_RGTC1_EXT (" + hexv(value) + ")";
        if (value == 0x8DBC) return "GL_COMPRESSED_SIGNED_RED_RGTC1_EXT (" + hexv(value) + ")";
        if (value == 0x8DBD) return "GL_COMPRESSED_RED_GREEN_RGTC2_EXT (" + hexv(value) + ")";
        if (value == 0x8DBE) return "GL_COMPRESSED_SIGNED_RED_GREEN_RGTC2_EXT (" + hexv(value) + ")";
        if (value == 0x8E8C) return "GL_COMPRESSED_RGBA_BPTC_UNORM_EXT (" + hexv(value) + ")";
        if (value == 0x8E8D) return "GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT (" + hexv(value) + ")";
        if (value == 0x8E8E) return "GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT (" + hexv(value) + ")";
        if (value == 0x8E8F) return "GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT (" + hexv(value) + ")";
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
        if (value == 0x9770) return "GL_SHADER_BINARY_HUAWEI (" + hexv(value) + ")";
    }
    if (category == "programBinaryFormats") {
        if (value == 0x8740) return "GL_Z400_BINARY_AMD (" + hexv(value) + ")";
        if (value == 0x8F61) return "GL_MALI_PROGRAM_BINARY_ARM (" + hexv(value) + ")";
        if (value == 0x9130) return "GL_SGX_PROGRAM_BINARY_IMG (" + hexv(value) + ")";
        if (value == 0x93A6) return "GL_PROGRAM_BINARY_ANGLE (" + hexv(value) + ")";
        if (value == 0x875F) return "GL_PROGRAM_BINARY_FORMAT_MESA (" + hexv(value) + ")";
        if (value == 0x9251) return "GL_SMAPHS30_PROGRAM_BINARY_DMP (" + hexv(value) + ")";
        if (value == 0x9252) return "GL_SMAPHS_PROGRAM_BINARY_DMP (" + hexv(value) + ")";
        if (value == 0x9253) return "GL_DMP_PROGRAM_BINARY_DMP (" + hexv(value) + ")";
        if (value == 0x9771) return "GL_PROGRAM_BINARY_HUAWEI (" + hexv(value) + ")";
    }
    return hexv(value);
}

static size_t boundedCStringLength(const char* s) {
    if (!s) return 0;
    for (size_t i = 0; i <= kMaxRuntimeStringBytes; ++i) if (s[i] == '\0') return i;
    return kMaxRuntimeStringBytes + 1;
}

static bool runtimeStringValid(const char* s) { return s != nullptr && boundedCStringLength(s) <= kMaxRuntimeStringBytes; }

static std::vector<std::string> splitExt(const char* s) {
    std::vector<std::string> out;
    if (!runtimeStringValid(s)) return out;
    const size_t length = boundedCStringLength(s);
    size_t i = 0;
    while (i < length) {
        while (i < length && std::isspace(static_cast<unsigned char>(s[i]))) ++i;
        const size_t start = i;
        while (i < length && !std::isspace(static_cast<unsigned char>(s[i]))) ++i;
        const size_t tokenLength = i - start;
        if (tokenLength == 0) continue;
        if (tokenLength > kMaxExtensionTokenBytes || out.size() >= static_cast<size_t>(kMaxGlEnumerationCount)) return {};
        out.emplace_back(s + start, tokenLength);
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

static void clearGlErrors() { for (int i = 0; i < 16; ++i) { if (glGetError() == GL_NO_ERROR) return; } }

struct QueryDiagnosticNative { std::string name; std::string status; std::string detail; };
static thread_local std::vector<QueryDiagnosticNative>* activeDiagnostics = nullptr;

static std::string glErrorHex(GLenum error) { std::ostringstream o; o << "0x" << std::uppercase << std::hex << static_cast<unsigned int>(error); return o.str(); }
static void diagnostic(const char* name, GLenum error) { if (!activeDiagnostics) return; const std::string status = error == GL_NO_ERROR ? "Available" : "Unavailable"; const std::string detail = error == GL_NO_ERROR ? "" : std::string("GL error ") + glErrorHex(error); auto it = std::find_if(activeDiagnostics->begin(), activeDiagnostics->end(), [&](const QueryDiagnosticNative& x) { return x.name == name; }); if (it == activeDiagnostics->end()) { activeDiagnostics->push_back({name, status, detail}); return; } if (it->status == "Available" && status == "Available") return; if (it->status != status || it->detail != detail) { it->status = "Unavailable"; it->detail = detail.empty() ? "Repeated query produced inconsistent runtime evidence" : detail; } }

static const char* queryGlString(GLenum name, const char* diagnosticName) {
    clearGlErrors();
    const auto* value = reinterpret_cast<const char*>(glGetString(name));
    const GLenum error = glGetError();
    const bool valid = error == GL_NO_ERROR && runtimeStringValid(value);
    diagnostic(diagnosticName, valid ? GL_NO_ERROR : error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
    return valid ? value : nullptr;
}

static std::pair<int, int> runtimeGlVersion(const char* text) {
    const auto parsed = parseGlVersion(text);
    if (versionCode(parsed) < 300) return parsed;
    GLint major = 0;
    GLint minor = 0;
    clearGlErrors();
    glGetIntegerv(GL_MAJOR_VERSION, &major);
    const GLenum majorError = glGetError();
    diagnostic("GL_MAJOR_VERSION", majorError);
    clearGlErrors();
    glGetIntegerv(GL_MINOR_VERSION, &minor);
    const GLenum minorError = glGetError();
    diagnostic("GL_MINOR_VERSION", minorError);
    if (majorError == GL_NO_ERROR && minorError == GL_NO_ERROR && major >= 3 && minor >= 0) return {major, minor};
    return parsed;
}


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

using GetQueryivExtProc = void (*)(GLenum, GLenum, GLint*);

static void addQueryCounterBitsExt(std::ostringstream& o, bool& first, GetQueryivExtProc fn, const char* name, GLenum target) {
    if (!fn) {
        if (activeDiagnostics) activeDiagnostics->push_back({name, "Unavailable", "glGetQueryivEXT unavailable"});
        return;
    }
    GLint v = 0;
    clearGlErrors();
    fn(target, 0x8864, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(std::to_string(v)) << '}';
}

static std::vector<std::string> glExtensions(int glCode) {
    std::vector<std::string> out;
    if (glCode >= 300) {
        GLint n = 0;
        clearGlErrors();
        glGetIntegerv(GL_NUM_EXTENSIONS, &n);
        GLenum error = glGetError();
        diagnostic("GL_NUM_EXTENSIONS", error == GL_NO_ERROR && n >= 0 && n <= kMaxGlEnumerationCount ? GL_NO_ERROR : error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
        if (error != GL_NO_ERROR || n < 0 || n > kMaxGlEnumerationCount) {
            diagnostic("GL_EXTENSIONS", error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
            return out;
        }
        out.reserve(static_cast<size_t>(n));
        for (GLint i = 0; i < n; ++i) {
            clearGlErrors();
            const auto* extension = reinterpret_cast<const char*>(glGetStringi(GL_EXTENSIONS, static_cast<GLuint>(i)));
            error = glGetError();
            if (error != GL_NO_ERROR || !runtimeStringValid(extension)) {
                out.clear();
                diagnostic("GL_EXTENSIONS", error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
                return out;
            }
            const size_t extensionLength = boundedCStringLength(extension);
            if (extensionLength > kMaxExtensionTokenBytes) {
                out.clear();
                diagnostic("GL_EXTENSIONS", GL_INVALID_VALUE);
                return out;
            }
            if (*extension) out.emplace_back(extension, extensionLength);
        }
        diagnostic("GL_EXTENSIONS", GL_NO_ERROR);
    } else {
        clearGlErrors();
        const auto* extensionString = reinterpret_cast<const char*>(glGetString(GL_EXTENSIONS));
        const GLenum error = glGetError();
        if (error != GL_NO_ERROR || !runtimeStringValid(extensionString)) {
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
    if (eglBindAPI(EGL_OPENGL_ES_API) != EGL_TRUE) return EGL_NO_CONTEXT;
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

extern "C" JNIEXPORT jstring JNICALL
Java_com_efishell_openglesscope_OpenGLESProbeService_nativeCollect(JNIEnv* env, jobject) {
    EGLDisplay d = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (d == EGL_NO_DISPLAY) { eglReleaseThread(); return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL display unavailable\"}"); }

    EGLint eglMajor = 0;
    EGLint eglMinor = 0;
    if (eglInitialize(d, &eglMajor, &eglMinor) != EGL_TRUE) { eglReleaseThread(); return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"eglInitialize failed\"}"); }

    EGLConfig cfg = nullptr;
    const char* rawDisplayExtensionText = eglQueryString(d, EGL_EXTENSIONS);
    const bool displayExtensionTextValid = runtimeStringValid(rawDisplayExtensionText);
    const char* displayExtensionText = displayExtensionTextValid ? rawDisplayExtensionText : nullptr;
    const auto displayExt = splitExt(displayExtensionText);
    const int eglCode = eglMajor * 100 + eglMinor * 10;
    EGLContext c = createBestContext(d, cfg, eglCode, displayExt);
    if (c == EGL_NO_CONTEXT || cfg == nullptr) {
        releaseEgl(d, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"OpenGL ES context creation failed\"}");
    }

    const EGLint pbAttrs[] = {EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE};
    EGLSurface s = eglCreatePbufferSurface(d, cfg, pbAttrs);
    if (s == EGL_NO_SURFACE) {
        releaseEgl(d, EGL_NO_SURFACE, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL pbuffer creation failed\"}");
    }
    if (eglMakeCurrent(d, s, s, c) != EGL_TRUE) {
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"eglMakeCurrent failed\"}");
    }

    std::vector<QueryDiagnosticNative> diagnostics;
    activeDiagnostics = &diagnostics;
    const char* glVendor = queryGlString(GL_VENDOR, "GL_VENDOR");
    const char* glRenderer = queryGlString(GL_RENDERER, "GL_RENDERER");
    const char* glVersion = queryGlString(GL_VERSION, "GL_VERSION");
    const char* glslVersion = queryGlString(GL_SHADING_LANGUAGE_VERSION, "GL_SHADING_LANGUAGE_VERSION");
    if (glVendor == nullptr || glRenderer == nullptr || glVersion == nullptr || glslVersion == nullptr) {
        activeDiagnostics = nullptr;
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"Required OpenGL ES identity strings were unavailable\"}");
    }
    const auto parsed = runtimeGlVersion(glVersion);
    const int glCode = versionCode(parsed);
    const auto glExt = glExtensions(glCode);
    const char* rawClientExtensionText = eglQueryString(EGL_NO_DISPLAY, EGL_EXTENSIONS);
    const char* clientExtensionText = runtimeStringValid(rawClientExtensionText) ? rawClientExtensionText : nullptr;
    const auto clientExt = splitExt(clientExtensionText);
    const char* rawEglVendorText = eglQueryString(d, EGL_VENDOR);
    const char* rawEglVersionText = eglQueryString(d, EGL_VERSION);
    const char* rawEglClientApisText = eglQueryString(d, EGL_CLIENT_APIS);
    const char* eglVendorText = runtimeStringValid(rawEglVendorText) ? rawEglVendorText : nullptr;
    const char* eglVersionText = runtimeStringValid(rawEglVersionText) ? rawEglVersionText : nullptr;
    const char* eglClientApisText = runtimeStringValid(rawEglClientApisText) ? rawEglClientApisText : nullptr;
    auto eglStringDetail = [](const char* raw, const char* valid) { return valid ? std::string() : raw ? std::string("Runtime string exceeded the 1 MiB safety bound") : std::string("eglQueryString returned null"); };
    diagnostics.push_back({"EGL_EXTENSIONS", displayExtensionText ? "Available" : "Unavailable", eglStringDetail(rawDisplayExtensionText, displayExtensionText)});
    diagnostics.push_back({"EGL_NO_DISPLAY/EGL_EXTENSIONS", clientExtensionText ? "Available" : "Unavailable", eglStringDetail(rawClientExtensionText, clientExtensionText)});
    diagnostics.push_back({"EGL_VENDOR", eglVendorText ? "Available" : "Unavailable", eglStringDetail(rawEglVendorText, eglVendorText)});
    diagnostics.push_back({"EGL_VERSION", eglVersionText ? "Available" : "Unavailable", eglStringDetail(rawEglVersionText, eglVersionText)});
    diagnostics.push_back({"EGL_CLIENT_APIS", eglClientApisText ? "Available" : "Unavailable", eglStringDetail(rawEglClientApisText, eglClientApisText)});

    const EGLenum boundApi = eglQueryAPI();
    const bool currentContext = eglGetCurrentContext() == c;
    const bool currentDisplay = eglGetCurrentDisplay() == d;
    const bool currentDrawSurface = eglGetCurrentSurface(EGL_DRAW) == s;
    const bool currentReadSurface = eglGetCurrentSurface(EGL_READ) == s;
    const auto contextConfigId = queryContextAttr(d, c, EGL_CONFIG_ID);
    const auto contextClientType = queryContextAttr(d, c, EGL_CONTEXT_CLIENT_TYPE);
    const auto contextClientVersion = queryContextAttr(d, c, EGL_CONTEXT_CLIENT_VERSION);
    const auto contextRenderBuffer = queryContextAttr(d, c, EGL_RENDER_BUFFER);
    const auto surfaceWidth = querySurfaceAttr(d, s, EGL_WIDTH);
    const auto surfaceHeight = querySurfaceAttr(d, s, EGL_HEIGHT);
    const auto surfaceRenderBuffer = querySurfaceAttr(d, s, EGL_RENDER_BUFFER);
    const auto surfaceSwapBehavior = querySurfaceAttr(d, s, EGL_SWAP_BEHAVIOR);
    const auto surfaceTextureFormat = querySurfaceAttr(d, s, EGL_TEXTURE_FORMAT);
    const auto surfaceTextureTarget = querySurfaceAttr(d, s, EGL_TEXTURE_TARGET);
    const auto surfaceMipmapTexture = querySurfaceAttr(d, s, EGL_MIPMAP_TEXTURE);
    const auto surfaceMipmapLevel = querySurfaceAttr(d, s, EGL_MIPMAP_LEVEL);
    const auto surfaceMultisampleResolve = eglCode >= 140 ? querySurfaceAttr(d, s, EGL_MULTISAMPLE_RESOLVE) : EglAttrResult{0, false, EGL_BAD_ATTRIBUTE};
    const std::pair<const char*, EglAttrResult> eglRuntimeAttributes[] = {
        {"EGL_CONFIG_ID", contextConfigId}, {"EGL_CONTEXT_CLIENT_TYPE", contextClientType}, {"EGL_CONTEXT_CLIENT_VERSION", contextClientVersion},
        {"EGL_RENDER_BUFFER/context", contextRenderBuffer}, {"EGL_WIDTH", surfaceWidth}, {"EGL_HEIGHT", surfaceHeight},
        {"EGL_RENDER_BUFFER/surface", surfaceRenderBuffer}, {"EGL_SWAP_BEHAVIOR", surfaceSwapBehavior}, {"EGL_TEXTURE_FORMAT", surfaceTextureFormat},
        {"EGL_TEXTURE_TARGET", surfaceTextureTarget}, {"EGL_MIPMAP_TEXTURE", surfaceMipmapTexture}, {"EGL_MIPMAP_LEVEL", surfaceMipmapLevel},
        {"EGL_MULTISAMPLE_RESOLVE", surfaceMultisampleResolve}
    };
    for (const auto& entry : eglRuntimeAttributes) {
        diagnostics.push_back({entry.first, entry.second.available ? "Available" : eglCode < 140 && std::string(entry.first) == "EGL_MULTISAMPLE_RESOLVE" ? "Not applicable" : "Unavailable", entry.second.available ? "" : eglCode < 140 && std::string(entry.first) == "EGL_MULTISAMPLE_RESOLVE" ? "Requires EGL 1.4+" : eglErrorDisplay(entry.second.error)});
    }
    diagnostics.push_back({"eglQueryAPI", boundApi != EGL_NONE ? "Available" : "Unavailable", boundApi != EGL_NONE ? "" : "eglQueryAPI returned EGL_NONE"});
    diagnostics.push_back({"EGL current bindings", currentContext && currentDisplay && currentDrawSurface && currentReadSurface ? "Available" : "Unavailable", currentContext && currentDisplay && currentDrawSurface && currentReadSurface ? "" : "The collector context/display/surface binding did not match the current EGL state"});

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
    o << "},\"eglRuntime\":{\"boundApi\":" << q(eglApiDisplay(boundApi))
      << ",\"configId\":" << (contextConfigId.available ? std::to_string(contextConfigId.value) : "null")
      << ",\"clientType\":" << (contextClientType.available ? q(eglEnumDisplay(contextClientType.value)) : "null")
      << ",\"clientVersion\":" << (contextClientVersion.available ? std::to_string(contextClientVersion.value) : "null")
      << ",\"renderBuffer\":" << (contextRenderBuffer.available ? q(eglEnumDisplay(contextRenderBuffer.value)) : "null")
      << ",\"currentContext\":" << (currentContext ? "true" : "false")
      << ",\"currentDisplay\":" << (currentDisplay ? "true" : "false")
      << ",\"currentDrawSurface\":" << (currentDrawSurface ? "true" : "false")
      << ",\"currentReadSurface\":" << (currentReadSurface ? "true" : "false")
      << ",\"surfaceWidth\":" << (surfaceWidth.available ? std::to_string(surfaceWidth.value) : "null")
      << ",\"surfaceHeight\":" << (surfaceHeight.available ? std::to_string(surfaceHeight.value) : "null")
      << ",\"surfaceRenderBuffer\":" << (surfaceRenderBuffer.available ? q(eglEnumDisplay(surfaceRenderBuffer.value)) : "null")
      << ",\"surfaceSwapBehavior\":" << (surfaceSwapBehavior.available ? q(eglEnumDisplay(surfaceSwapBehavior.value)) : "null")
      << ",\"surfaceTextureFormat\":" << (surfaceTextureFormat.available ? q(eglEnumDisplay(surfaceTextureFormat.value)) : "null")
      << ",\"surfaceTextureTarget\":" << (surfaceTextureTarget.available ? q(eglEnumDisplay(surfaceTextureTarget.value)) : "null")
      << ",\"surfaceMipmapTexture\":" << (surfaceMipmapTexture.available ? std::to_string(surfaceMipmapTexture.value) : "null")
      << ",\"surfaceMipmapLevel\":" << (surfaceMipmapLevel.available ? std::to_string(surfaceMipmapLevel.value) : "null")
      << ",\"surfaceMultisampleResolve\":" << (surfaceMultisampleResolve.available ? q(eglEnumDisplay(surfaceMultisampleResolve.value)) : "null")
      << ",\"unavailableAttributes\":[";
    bool eglRuntimeFailureFirst = true;
    for (const auto& entry : eglRuntimeAttributes) {
        if (entry.second.available || (eglCode < 140 && std::string(entry.first) == "EGL_MULTISAMPLE_RESOLVE")) continue;
        if (!eglRuntimeFailureFirst) o << ',';
        eglRuntimeFailureFirst = false;
        o << "{\"name\":" << q(entry.first) << ",\"error\":" << q(eglErrorDisplay(entry.second.error)) << '}';
    }
    o << "]},\"extensions\":";
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
    if (glCode < 320 && hasExt(glExt, "GL_KHR_debug")) {
        addLimit(o, first, "GL_MAX_DEBUG_MESSAGE_LENGTH", 0x9143);
        addLimit(o, first, "GL_MAX_DEBUG_LOGGED_MESSAGES", 0x9144);
        addLimit(o, first, "GL_MAX_DEBUG_GROUP_STACK_DEPTH", 0x826C);
        addLimit(o, first, "GL_MAX_LABEL_LENGTH", 0x82E8);
    }
    if (hasExt(glExt, "GL_EXT_disjoint_timer_query")) {
        const auto getQueryivExt = reinterpret_cast<GetQueryivExtProc>(eglGetProcAddress("glGetQueryivEXT"));
        addQueryCounterBitsExt(o, first, getQueryivExt, "GL_TIME_ELAPSED_EXT_QUERY_COUNTER_BITS", 0x88BF);
        addQueryCounterBitsExt(o, first, getQueryivExt, "GL_TIMESTAMP_EXT_QUERY_COUNTER_BITS", 0x8E28);
    }
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
    if (hasExt(glExt, "GL_KHR_shader_subgroup")) {
        addLimit(o, first, "GL_SUBGROUP_SIZE_KHR", 0x9532);
        addHexLimit(o, first, "GL_SUBGROUP_SUPPORTED_STAGES_KHR", 0x9533);
        addHexLimit(o, first, "GL_SUBGROUP_SUPPORTED_FEATURES_KHR", 0x9534);
        addBooleanLimit(o, first, "GL_SUBGROUP_QUAD_ALL_STAGES_KHR", 0x9535);
    }
    if (hasExt(glExt, "GL_EXT_window_rectangles")) {
        addLimit(o, first, "GL_MAX_WINDOW_RECTANGLES_EXT", 0x8F14);
    }
    if (hasExt(glExt, "GL_OES_viewport_array")) {
        addLimit(o, first, "GL_MAX_VIEWPORTS_OES", 0x825B);
        addLimit(o, first, "GL_VIEWPORT_SUBPIXEL_BITS_OES", 0x825C);
        addFloatLimit2(o, first, "GL_VIEWPORT_BOUNDS_RANGE_OES", 0x825D);
        addHexLimit(o, first, "GL_VIEWPORT_INDEX_PROVOKING_VERTEX_OES", 0x825F);
    }
    if (hasExt(glExt, "GL_EXT_shader_pixel_local_storage")) {
        addLimit(o, first, "GL_MAX_SHADER_PIXEL_LOCAL_STORAGE_FAST_SIZE_EXT", 0x8F63);
        addLimit(o, first, "GL_MAX_SHADER_PIXEL_LOCAL_STORAGE_SIZE_EXT", 0x8F67);
    }
    if (hasExt(glExt, "GL_EXT_shader_pixel_local_storage2")) {
        addLimit(o, first, "GL_MAX_SHADER_COMBINED_LOCAL_STORAGE_FAST_SIZE_EXT", 0x9650);
        addLimit(o, first, "GL_MAX_SHADER_COMBINED_LOCAL_STORAGE_SIZE_EXT", 0x9651);
    }
    if (glCode < 320 && hasExt(glExt, "GL_OES_sample_shading")) addFloatLimit(o, first, "GL_MIN_SAMPLE_SHADING_VALUE_OES", 0x8C37);
    if (hasExt(glExt, "GL_EXT_sparse_texture")) {
        addLimit(o, first, "GL_MAX_SPARSE_TEXTURE_SIZE_EXT", 0x9198);
        addLimit(o, first, "GL_MAX_SPARSE_3D_TEXTURE_SIZE_EXT", 0x9199);
        addLimit(o, first, "GL_MAX_SPARSE_ARRAY_TEXTURE_LAYERS_EXT", 0x919A);
        addBooleanLimit(o, first, "GL_SPARSE_TEXTURE_FULL_ARRAY_CUBE_MIPMAPS_EXT", 0x91A9);
    }
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
        addLimit(o, first, "GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT", GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
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
        addLimit(o, first, "GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT", GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);
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

    auto appendEnumArray = [&](const char* key, const char* countName, GLenum countEnum, const char* valuesName, GLenum valuesEnum) {
        GLint count = 0;
        clearGlErrors();
        glGetIntegerv(countEnum, &count);
        const GLenum countError = glGetError();
        const GLenum normalizedCountError = countError == GL_NO_ERROR && count >= 0 && count <= kMaxGlEnumerationCount ? GL_NO_ERROR : countError == GL_NO_ERROR ? GL_INVALID_VALUE : countError;
        diagnostic(countName, normalizedCountError);
        std::vector<GLint> values;
        GLenum valueError = GL_NO_ERROR;
        if (normalizedCountError == GL_NO_ERROR) {
            values.resize(static_cast<size_t>(count));
            if (count > 0) {
                clearGlErrors();
                glGetIntegerv(valuesEnum, values.data());
                valueError = glGetError();
                if (valueError != GL_NO_ERROR) values.clear();
                diagnostic(valuesName, valueError);
            } else {
                if (activeDiagnostics) activeDiagnostics->push_back({valuesName, "Not applicable", "Count is zero"});
            }
        } else {
            if (activeDiagnostics) activeDiagnostics->push_back({valuesName, "Unavailable", std::string(countName) + " unavailable"});
            valueError = normalizedCountError;
        }
        diagnostic(key, normalizedCountError != GL_NO_ERROR ? normalizedCountError : valueError);
        o << ",\"" << key << "\":[";
        for (size_t i = 0; i < values.size(); ++i) {
            if (i) o << ',';
            o << q(enumDisplay(values[i], key));
        }
        o << ']';
    };

    appendEnumArray("shaderBinaryFormats", "GL_NUM_SHADER_BINARY_FORMATS", GL_NUM_SHADER_BINARY_FORMATS, "GL_SHADER_BINARY_FORMATS", GL_SHADER_BINARY_FORMATS);
    if (glCode >= 300 || hasExt(glExt, "GL_OES_get_program_binary")) {
        appendEnumArray("programBinaryFormats", "GL_NUM_PROGRAM_BINARY_FORMATS", GL_NUM_PROGRAM_BINARY_FORMATS, "GL_PROGRAM_BINARY_FORMATS", GL_PROGRAM_BINARY_FORMATS);
    } else {
        diagnostics.push_back({"programBinaryFormats", "Not applicable", "Requires OpenGL ES 3.0 or GL_OES_get_program_binary"});
        o << ",\"programBinaryFormats\":[]";
    }

    GLint nfmt = 0;
    clearGlErrors();
    glGetIntegerv(GL_NUM_COMPRESSED_TEXTURE_FORMATS, &nfmt);
    const GLenum compressedCountError = glGetError();
    const GLenum normalizedCompressedCountError = compressedCountError == GL_NO_ERROR && nfmt >= 0 && nfmt <= kMaxGlEnumerationCount ? GL_NO_ERROR : compressedCountError == GL_NO_ERROR ? GL_INVALID_VALUE : compressedCountError;
    diagnostic("GL_NUM_COMPRESSED_TEXTURE_FORMATS", normalizedCompressedCountError);
    std::vector<GLint> fmts;
    GLenum compressedValuesError = GL_NO_ERROR;
    if (normalizedCompressedCountError == GL_NO_ERROR) {
        fmts.resize(static_cast<size_t>(nfmt));
        if (nfmt > 0) {
            clearGlErrors();
            glGetIntegerv(GL_COMPRESSED_TEXTURE_FORMATS, fmts.data());
            compressedValuesError = glGetError();
            if (compressedValuesError != GL_NO_ERROR) fmts.clear();
            diagnostic("GL_COMPRESSED_TEXTURE_FORMATS", compressedValuesError);
        } else {
            if (activeDiagnostics) activeDiagnostics->push_back({"GL_COMPRESSED_TEXTURE_FORMATS", "Not applicable", "Count is zero"});
        }
    } else {
        if (activeDiagnostics) activeDiagnostics->push_back({"GL_COMPRESSED_TEXTURE_FORMATS", "Unavailable", "GL_NUM_COMPRESSED_TEXTURE_FORMATS unavailable"});
        compressedValuesError = normalizedCompressedCountError;
    }
    diagnostic("compressedFormats", normalizedCompressedCountError != GL_NO_ERROR ? normalizedCompressedCountError : compressedValuesError);
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
    if (eglGetConfigs(d, nullptr, 0, &totalConfigs) != EGL_TRUE || totalConfigs <= 0 || totalConfigs > kMaxEglConfigCount) {
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL configuration enumeration failed or exceeded the 4096-entry safety bound\"}");
    }
    const EGLint configCapacity = totalConfigs;
    std::vector<EGLConfig> configs(static_cast<size_t>(configCapacity));
    if (eglGetConfigs(d, configs.data(), configCapacity, &totalConfigs) != EGL_TRUE || totalConfigs <= 0 || totalConfigs > configCapacity || totalConfigs > kMaxEglConfigCount) {
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL configuration data could not be read safely\"}");
    }
    o << ",\"eglConfigs\":[";
    bool configFirst = true;
    for (EGLint i = 0; i < totalConfigs; ++i) {
        const EGLConfig ec = configs[static_cast<size_t>(i)];
        const auto idResult = queryConfigAttr(d, ec, EGL_CONFIG_ID);
        if (!idResult.available) {
            releaseEgl(d, s, c);
            return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"An EGL configuration could not be identified\"}");
        }
        const EGLint id = idResult.value;
        const std::pair<const char*, EglAttrResult> attrs[] = {
            {"red", queryConfigAttr(d, ec, EGL_RED_SIZE)}, {"green", queryConfigAttr(d, ec, EGL_GREEN_SIZE)}, {"blue", queryConfigAttr(d, ec, EGL_BLUE_SIZE)},
            {"alpha", queryConfigAttr(d, ec, EGL_ALPHA_SIZE)}, {"depth", queryConfigAttr(d, ec, EGL_DEPTH_SIZE)}, {"stencil", queryConfigAttr(d, ec, EGL_STENCIL_SIZE)},
            {"sampleBuffers", queryConfigAttr(d, ec, EGL_SAMPLE_BUFFERS)}, {"samples", queryConfigAttr(d, ec, EGL_SAMPLES)}, {"surfaceType", queryConfigAttr(d, ec, EGL_SURFACE_TYPE)},
            {"renderableType", queryConfigAttr(d, ec, EGL_RENDERABLE_TYPE)}, {"conformant", queryConfigAttr(d, ec, EGL_CONFORMANT)}, {"configCaveat", queryConfigAttr(d, ec, EGL_CONFIG_CAVEAT)},
            {"colorBufferType", queryConfigAttr(d, ec, EGL_COLOR_BUFFER_TYPE)}, {"level", queryConfigAttr(d, ec, EGL_LEVEL)}, {"nativeRenderable", queryConfigAttr(d, ec, EGL_NATIVE_RENDERABLE)},
            {"nativeVisualId", queryConfigAttr(d, ec, EGL_NATIVE_VISUAL_ID)}, {"minSwapInterval", queryConfigAttr(d, ec, EGL_MIN_SWAP_INTERVAL)}, {"maxSwapInterval", queryConfigAttr(d, ec, EGL_MAX_SWAP_INTERVAL)},
            {"bufferSize", queryConfigAttr(d, ec, EGL_BUFFER_SIZE)}, {"luminanceSize", queryConfigAttr(d, ec, EGL_LUMINANCE_SIZE)}, {"alphaMaskSize", queryConfigAttr(d, ec, EGL_ALPHA_MASK_SIZE)},
            {"bindToTextureRgb", queryConfigAttr(d, ec, EGL_BIND_TO_TEXTURE_RGB)}, {"bindToTextureRgba", queryConfigAttr(d, ec, EGL_BIND_TO_TEXTURE_RGBA)},
            {"maxPbufferWidth", queryConfigAttr(d, ec, EGL_MAX_PBUFFER_WIDTH)}, {"maxPbufferHeight", queryConfigAttr(d, ec, EGL_MAX_PBUFFER_HEIGHT)}, {"maxPbufferPixels", queryConfigAttr(d, ec, EGL_MAX_PBUFFER_PIXELS)},
            {"nativeVisualType", queryConfigAttr(d, ec, EGL_NATIVE_VISUAL_TYPE)}, {"transparentType", queryConfigAttr(d, ec, EGL_TRANSPARENT_TYPE)},
            {"transparentRed", queryConfigAttr(d, ec, EGL_TRANSPARENT_RED_VALUE)}, {"transparentGreen", queryConfigAttr(d, ec, EGL_TRANSPARENT_GREEN_VALUE)}, {"transparentBlue", queryConfigAttr(d, ec, EGL_TRANSPARENT_BLUE_VALUE)}
        };
        auto attr = [&](const char* name) -> EglAttrResult { for (const auto& item : attrs) if (std::string(item.first) == name) return item.second; return {0, false, EGL_BAD_ATTRIBUTE}; };
        const auto red = attr("red"), green = attr("green"), blue = attr("blue"), alpha = attr("alpha"), depth = attr("depth"), stencil = attr("stencil");
        const auto sampleBuffers = attr("sampleBuffers"), samples = attr("samples"), surfaceType = attr("surfaceType"), renderableType = attr("renderableType"), conformant = attr("conformant");
        const auto caveat = attr("configCaveat"), colorBufferType = attr("colorBufferType"), level = attr("level"), nativeRenderable = attr("nativeRenderable"), nativeVisualId = attr("nativeVisualId");
        const auto minSwap = attr("minSwapInterval"), maxSwap = attr("maxSwapInterval"), bufferSize = attr("bufferSize"), luminanceSize = attr("luminanceSize"), alphaMaskSize = attr("alphaMaskSize");
        const auto bindRgb = attr("bindToTextureRgb"), bindRgba = attr("bindToTextureRgba"), maxPbufferWidth = attr("maxPbufferWidth"), maxPbufferHeight = attr("maxPbufferHeight"), maxPbufferPixels = attr("maxPbufferPixels");
        const auto nativeVisualType = attr("nativeVisualType"), transparentType = attr("transparentType"), transparentRed = attr("transparentRed"), transparentGreen = attr("transparentGreen"), transparentBlue = attr("transparentBlue");
        const bool hasRecordable = hasExt(displayExt, "EGL_ANDROID_recordable");
        const bool hasFramebufferTarget = hasExt(displayExt, "EGL_ANDROID_framebuffer_target");
        const bool hasFloatComponents = hasExt(displayExt, "EGL_EXT_pixel_format_float");
        const auto recordable = hasRecordable ? queryConfigAttr(d, ec, EGL_RECORDABLE_ANDROID_VALUE) : EglAttrResult{0, false, EGL_SUCCESS};
        const auto framebufferTarget = hasFramebufferTarget ? queryConfigAttr(d, ec, EGL_FRAMEBUFFER_TARGET_ANDROID_VALUE) : EglAttrResult{0, false, EGL_SUCCESS};
        const auto colorComponentType = hasFloatComponents ? queryConfigAttr(d, ec, EGL_COLOR_COMPONENT_TYPE_EXT_VALUE) : EglAttrResult{0, false, EGL_SUCCESS};
        if (!configFirst) o << ',';
        configFirst = false;
        o << "{\"id\":" << id
          << ",\"red\":" << (red.available ? std::to_string(red.value) : "null")
          << ",\"green\":" << (green.available ? std::to_string(green.value) : "null")
          << ",\"blue\":" << (blue.available ? std::to_string(blue.value) : "null")
          << ",\"alpha\":" << (alpha.available ? std::to_string(alpha.value) : "null")
          << ",\"depth\":" << (depth.available ? std::to_string(depth.value) : "null")
          << ",\"stencil\":" << (stencil.available ? std::to_string(stencil.value) : "null")
          << ",\"sampleBuffers\":" << (sampleBuffers.available ? std::to_string(sampleBuffers.value) : "null")
          << ",\"samples\":" << (samples.available ? std::to_string(samples.value) : "null")
          << ",\"surfaceType\":" << (surfaceType.available ? q(hexv(surfaceType.value)) : "null")
          << ",\"renderableType\":" << (renderableType.available ? q(hexv(renderableType.value)) : "null")
          << ",\"conformant\":" << (conformant.available ? q(hexv(conformant.value)) : "null")
          << ",\"configCaveat\":" << (caveat.available ? q(hexv(caveat.value)) : "null")
          << ",\"colorBufferType\":" << (colorBufferType.available ? q(hexv(colorBufferType.value)) : "null")
          << ",\"level\":" << (level.available ? std::to_string(level.value) : "null")
          << ",\"nativeRenderable\":" << (nativeRenderable.available ? std::to_string(nativeRenderable.value) : "null")
          << ",\"nativeVisualId\":" << (nativeVisualId.available ? std::to_string(nativeVisualId.value) : "null")
          << ",\"minSwapInterval\":" << (minSwap.available ? std::to_string(minSwap.value) : "null")
          << ",\"maxSwapInterval\":" << (maxSwap.available ? std::to_string(maxSwap.value) : "null")
          << ",\"bufferSize\":" << (bufferSize.available ? std::to_string(bufferSize.value) : "null")
          << ",\"luminanceSize\":" << (luminanceSize.available ? std::to_string(luminanceSize.value) : "null")
          << ",\"alphaMaskSize\":" << (alphaMaskSize.available ? std::to_string(alphaMaskSize.value) : "null")
          << ",\"bindToTextureRgb\":" << (bindRgb.available ? std::to_string(bindRgb.value) : "null")
          << ",\"bindToTextureRgba\":" << (bindRgba.available ? std::to_string(bindRgba.value) : "null")
          << ",\"maxPbufferWidth\":" << (maxPbufferWidth.available ? std::to_string(maxPbufferWidth.value) : "null")
          << ",\"maxPbufferHeight\":" << (maxPbufferHeight.available ? std::to_string(maxPbufferHeight.value) : "null")
          << ",\"maxPbufferPixels\":" << (maxPbufferPixels.available ? std::to_string(maxPbufferPixels.value) : "null")
          << ",\"nativeVisualType\":" << (nativeVisualType.available ? std::to_string(nativeVisualType.value) : "null")
          << ",\"transparentType\":" << (transparentType.available ? q(hexv(transparentType.value)) : "null")
          << ",\"transparentRed\":" << (transparentRed.available ? std::to_string(transparentRed.value) : "null")
          << ",\"transparentGreen\":" << (transparentGreen.available ? std::to_string(transparentGreen.value) : "null")
          << ",\"transparentBlue\":" << (transparentBlue.available ? std::to_string(transparentBlue.value) : "null")
          << ",\"recordableAndroid\":" << (hasRecordable && recordable.available ? std::to_string(recordable.value) : "null")
          << ",\"framebufferTargetAndroid\":" << (hasFramebufferTarget && framebufferTarget.available ? std::to_string(framebufferTarget.value) : "null")
          << ",\"colorComponentTypeExt\":" << (hasFloatComponents && colorComponentType.available ? q(eglEnumDisplay(colorComponentType.value)) : "null")
          << ",\"unavailableAttributes\":[";
        bool unavailableFirst = true;
        for (const auto& item : attrs) {
            if (item.second.available) continue;
            if (!unavailableFirst) o << ',';
            unavailableFirst = false;
            o << "{\"name\":" << q(item.first) << ",\"error\":" << q(eglErrorDisplay(item.second.error)) << '}';
        }
        const std::pair<const char*, EglAttrResult> extensionAttrs[] = {{"EGL_RECORDABLE_ANDROID", recordable}, {"EGL_FRAMEBUFFER_TARGET_ANDROID", framebufferTarget}, {"EGL_COLOR_COMPONENT_TYPE_EXT", colorComponentType}};
        const bool extensionApplies[] = {hasRecordable, hasFramebufferTarget, hasFloatComponents};
        for (size_t extensionIndex = 0; extensionIndex < 3; ++extensionIndex) {
            if (!extensionApplies[extensionIndex] || extensionAttrs[extensionIndex].second.available) continue;
            if (!unavailableFirst) o << ',';
            unavailableFirst = false;
            o << "{\"name\":" << q(extensionAttrs[extensionIndex].first) << ",\"error\":" << q(eglErrorDisplay(extensionAttrs[extensionIndex].second.error)) << '}';
        }
        o << "]}";
    }
    o << "]}";

    releaseEgl(d, s, c);
    const std::string result = o.str();
    return env->NewStringUTF(result.c_str());
}

static std::string shaderInfoLog(GLuint shader) {
    GLint length = 0;
    glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &length);
    if (length <= 1) return "";
    if (length > kMaxInfoLogBytes) return "Shader info log exceeded the 1 MiB safety bound";
    std::vector<char> text(static_cast<size_t>(length));
    glGetShaderInfoLog(shader, length, nullptr, text.data());
    return std::string(text.data());
}

static std::string programInfoLog(GLuint program) {
    GLint length = 0;
    glGetProgramiv(program, GL_INFO_LOG_LENGTH, &length);
    if (length <= 1) return "";
    if (length > kMaxInfoLogBytes) return "Program info log exceeded the 1 MiB safety bound";
    std::vector<char> text(static_cast<size_t>(length));
    glGetProgramInfoLog(program, length, nullptr, text.data());
    return std::string(text.data());
}


static thread_local bool selfTestDebugCallbackSeen = false;
static void selfTestDebugCallback(GLenum, GLenum, GLuint, GLenum, GLsizei, const GLchar*, const void*) { selfTestDebugCallbackSeen = true; }

extern "C" JNIEXPORT jstring JNICALL
Java_com_efishell_openglesscope_OpenGLESProbeService_nativeSelfTest(JNIEnv* env, jobject) {
    EGLDisplay d = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (d == EGL_NO_DISPLAY) { eglReleaseThread(); return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL display unavailable\",\"tests\":[]}"); }
    EGLint eglMajor = 0;
    EGLint eglMinor = 0;
    if (eglInitialize(d, &eglMajor, &eglMinor) != EGL_TRUE) { eglReleaseThread(); return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"eglInitialize failed\",\"tests\":[]}"); }
    EGLConfig cfg = nullptr;
    const auto displayExt = splitExt(eglQueryString(d, EGL_EXTENSIONS));
    EGLContext c = createBestContext(d, cfg, eglMajor * 100 + eglMinor * 10, displayExt);
    if (c == EGL_NO_CONTEXT || cfg == nullptr) {
        releaseEgl(d, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"OpenGL ES context creation failed\",\"tests\":[]}");
    }
    const EGLint pbAttrs[] = {EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE};
    EGLSurface surface = eglCreatePbufferSurface(d, cfg, pbAttrs);
    if (surface == EGL_NO_SURFACE || eglMakeCurrent(d, surface, surface, c) != EGL_TRUE) {
        releaseEgl(d, surface, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"OpenGL ES self-test context could not be made current\",\"tests\":[]}");
    }
    const char* vendorText = queryGlString(GL_VENDOR, "GL_VENDOR");
    const char* rendererText = queryGlString(GL_RENDERER, "GL_RENDERER");
    const char* versionText = queryGlString(GL_VERSION, "GL_VERSION");
    if (!vendorText || !rendererText || !versionText) {
        releaseEgl(d, surface, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"Self-test OpenGL ES identity strings were unavailable\",\"tests\":[]}");
    }
    const std::string runtimeVendor = vendorText;
    const std::string runtimeRenderer = rendererText;
    const std::string runtimeVersion = versionText;
    const auto parsed = runtimeGlVersion(versionText);
    const int glCode = versionCode(parsed);
    const auto extensions = glExtensions(glCode);
    auto hasExt = [&](const char* name) { return std::find(extensions.begin(), extensions.end(), name) != extensions.end(); };
    std::vector<std::string> tests;
    const char* vertexSource = "attribute vec4 aPosition; void main(){gl_Position=aPosition;}";
    const char* fragmentSource = "precision mediump float; void main(){gl_FragColor=vec4(1.0,0.0,1.0,1.0);}";
    GLuint vertex = glCreateShader(GL_VERTEX_SHADER);
    GLuint fragment = glCreateShader(GL_FRAGMENT_SHADER);
    if (vertex == 0 || fragment == 0) {
        if (vertex != 0) glDeleteShader(vertex);
        if (fragment != 0) glDeleteShader(fragment);
        releaseEgl(d, surface, c);
        return env->NewStringUTF("{\"status\":\"completed_with_failures\",\"reason\":\"Shader object creation failed\",\"tests\":[{\"name\":\"Shader object creation\",\"status\":\"FAIL\",\"detail\":\"glCreateShader returned zero\"}]}");
    }
    glShaderSource(vertex, 1, &vertexSource, nullptr);
    glShaderSource(fragment, 1, &fragmentSource, nullptr);
    glCompileShader(vertex);
    glCompileShader(fragment);
    GLint vertexOk = GL_FALSE;
    GLint fragmentOk = GL_FALSE;
    glGetShaderiv(vertex, GL_COMPILE_STATUS, &vertexOk);
    glGetShaderiv(fragment, GL_COMPILE_STATUS, &fragmentOk);
    tests.push_back(std::string("{\"name\":\"Minimal vertex shader compile\",\"status\":") + q(vertexOk == GL_TRUE ? "PASS" : "FAIL") + ",\"detail\":" + q(shaderInfoLog(vertex)) + "}");
    tests.push_back(std::string("{\"name\":\"Minimal fragment shader compile\",\"status\":") + q(fragmentOk == GL_TRUE ? "PASS" : "FAIL") + ",\"detail\":" + q(shaderInfoLog(fragment)) + "}");
    GLuint program = glCreateProgram();
    if (program == 0) {
        glDeleteShader(vertex);
        glDeleteShader(fragment);
        releaseEgl(d, surface, c);
        return env->NewStringUTF("{\"status\":\"completed_with_failures\",\"reason\":\"Program object creation failed\",\"tests\":[{\"name\":\"Program object creation\",\"status\":\"FAIL\",\"detail\":\"glCreateProgram returned zero\"}]}");
    }
    if (vertexOk == GL_TRUE && fragmentOk == GL_TRUE) {
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        if (glCode >= 300) glProgramParameteri(program, GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);
        glLinkProgram(program);
    }
    GLint linkOk = GL_FALSE;
    glGetProgramiv(program, GL_LINK_STATUS, &linkOk);
    tests.push_back(std::string("{\"name\":\"Minimal program link\",\"status\":") + q(linkOk == GL_TRUE ? "PASS" : "FAIL") + ",\"detail\":" + q(programInfoLog(program)) + "}");
    if (linkOk == GL_TRUE && (glCode >= 300 || hasExt("GL_OES_get_program_binary"))) {
        GLint length = 0;
        glGetProgramiv(program, GL_PROGRAM_BINARY_LENGTH, &length);
        if (length > 0 && length <= kMaxProgramBinaryBytes) {
            std::vector<unsigned char> binary(static_cast<size_t>(length));
            GLsizei written = 0;
            GLenum format = 0;
            bool roundTripAttempted = false;
            bool roundTripPassed = false;
            std::string detail;
            if (glCode >= 300) {
                glGetProgramBinary(program, length, &written, &format, binary.data());
                if (written > 0 && written <= length) {
                    GLuint restored = glCreateProgram();
                    if (restored != 0) glProgramBinary(restored, format, binary.data(), written);
                    GLint restoredOk = GL_FALSE;
                    if (restored != 0) glGetProgramiv(restored, GL_LINK_STATUS, &restoredOk);
                    roundTripAttempted = true;
                    roundTripPassed = restoredOk == GL_TRUE;
                    detail = restored != 0 ? programInfoLog(restored) : "glCreateProgram returned zero";
                    if (restored != 0) glDeleteProgram(restored);
                }
            } else {
                using GetProgramBinaryOES = void (*)(GLuint, GLsizei, GLsizei*, GLenum*, void*);
                using ProgramBinaryOES = void (*)(GLuint, GLenum, const void*, GLint);
                auto getBinary = reinterpret_cast<GetProgramBinaryOES>(eglGetProcAddress("glGetProgramBinaryOES"));
                auto setBinary = reinterpret_cast<ProgramBinaryOES>(eglGetProcAddress("glProgramBinaryOES"));
                if (getBinary && setBinary) {
                    getBinary(program, length, &written, &format, binary.data());
                    if (written > 0 && written <= length) {
                        GLuint restored = glCreateProgram();
                        if (restored != 0) setBinary(restored, format, binary.data(), written);
                        GLint restoredOk = GL_FALSE;
                        if (restored != 0) glGetProgramiv(restored, GL_LINK_STATUS, &restoredOk);
                        roundTripAttempted = true;
                        roundTripPassed = restoredOk == GL_TRUE;
                        detail = restored != 0 ? programInfoLog(restored) : "glCreateProgram returned zero";
                        if (restored != 0) glDeleteProgram(restored);
                    }
                }
            }
            tests.push_back(std::string("{\"name\":\"Program binary round-trip\",\"status\":") + q(roundTripAttempted ? (roundTripPassed ? "PASS" : "FAIL") : "UNAVAILABLE") + ",\"detail\":" + q(detail) + "}");
        } else {
            tests.push_back("{\"name\":\"Program binary round-trip\",\"status\":\"UNAVAILABLE\",\"detail\":\"Program binary length was unavailable or exceeded the 8 MiB self-test bound.\"}");
        }
    } else {
        tests.push_back("{\"name\":\"Program binary round-trip\",\"status\":\"NOT_APPLICABLE\",\"detail\":\"Program-binary runtime evidence is not available.\"}");
    }
    if (hasExt("GL_KHR_debug") || glCode >= 320) {
        using DebugCallback = void (*)(GLenum, GLenum, GLuint, GLenum, GLsizei, const GLchar*, const void*);
        using DebugMessageCallback = void (*)(DebugCallback, const void*);
        using DebugMessageInsert = void (*)(GLenum, GLenum, GLuint, GLenum, GLsizei, const GLchar*);
        auto callback = reinterpret_cast<DebugMessageCallback>(eglGetProcAddress(hasExt("GL_KHR_debug") ? "glDebugMessageCallbackKHR" : "glDebugMessageCallback"));
        if (!callback) callback = reinterpret_cast<DebugMessageCallback>(eglGetProcAddress("glDebugMessageCallback"));
        auto insert = reinterpret_cast<DebugMessageInsert>(eglGetProcAddress(hasExt("GL_KHR_debug") ? "glDebugMessageInsertKHR" : "glDebugMessageInsert"));
        if (!insert) insert = reinterpret_cast<DebugMessageInsert>(eglGetProcAddress("glDebugMessageInsert"));
        if (callback && insert) {
            clearGlErrors();
            selfTestDebugCallbackSeen = false;
            callback(selfTestDebugCallback, nullptr);
            glEnable(GL_DEBUG_OUTPUT);
            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
            const char message[] = "OpenGLESScope self-test";
            insert(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_MARKER, 1, GL_DEBUG_SEVERITY_NOTIFICATION, static_cast<GLsizei>(sizeof(message) - 1), message);
            const GLenum error = glGetError();
            const bool passed = error == GL_NO_ERROR && selfTestDebugCallbackSeen;
            tests.push_back(std::string("{\"name\":\"KHR_debug callback and insertion\",\"status\":") + q(passed ? "PASS" : "FAIL") + ",\"detail\":" + q(passed ? "Debug callback observed the inserted application message." : error == GL_NO_ERROR ? "Debug insertion completed but the callback was not observed." : "Debug callback/insertion generated a GL error.") + "}");
            callback(nullptr, nullptr);
        } else {
            tests.push_back("{\"name\":\"KHR_debug callback and insertion\",\"status\":\"UNAVAILABLE\",\"detail\":\"The required debug callback or insertion entry point was unavailable.\"}");
        }
    } else {
        tests.push_back("{\"name\":\"KHR_debug message insertion\",\"status\":\"NOT_APPLICABLE\",\"detail\":\"GL_KHR_debug is not exposed by this runtime.\"}");
    }
    glDeleteProgram(program);
    glDeleteShader(vertex);
    glDeleteShader(fragment);
    releaseEgl(d, surface, c);
    bool anyFail = false;
    for (const auto& test : tests) if (test.find("\"status\":\"FAIL\"") != std::string::npos) anyFail = true;
    std::ostringstream out;
    out << "{\"status\":" << q(anyFail ? "completed_with_failures" : "completed") << ",\"vendor\":" << q(runtimeVendor) << ",\"renderer\":" << q(runtimeRenderer) << ",\"runtimeVersion\":" << q(runtimeVersion) << ",\"tests\":[";
    for (size_t i = 0; i < tests.size(); ++i) { if (i) out << ','; out << tests[i]; }
    out << "]}";
    return env->NewStringUTF(out.str().c_str());
}
