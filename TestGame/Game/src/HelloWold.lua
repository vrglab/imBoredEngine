local Debug = imBoredEngine.Debug()
local Application = imBoredEngine.Application()

function init()
    if Application.isInEditor then
        Debug.logWarn("Lua knows it's in the editor")
    else
        Debug.log(Application.runtimePath)
        Debug.log(Application.appAuthor)
    end
end