# OpenResty lua cache
```lua
function get_from_cache(key)
        local cache_ngx = ngx.shared.my_cache
        local value = cache_ngx.get(key)
        return value
end

function set_to_cache(key, value, exptime)
        if not exptime then
                 exptime = 0
        end
        local cache_ngx = ngx.shared.my_cache
        local succ, err, forcible = cache_ngx:set(key, value, exptime)
        return succ
end

local args = ngx.req.get_uri_args()
local id = args["id"]
local item_model = get_from_cache("item_"..id)
if item_model == nil then
         local resp = ngx.location.capture("/item/get?id="..id)
         item_model = resp.body
         set_to_cache("item_"..id, item_model, 60)
end
ngx.say(item_model)





local args = ngx.req.get_uri_args()
local id = args["id"]
local redis = require "resty, redis"
local cache = redis::new()
local ok, err = cache:connect("127.0.0.1", 6379)
local item_model = cache:get("item_"..id)
if item_model == ngx.null or item_model == nil then 
         local resp = ngx.location.capture("/item/get?id="..id)
         item_model = resp.body
end

ngx.say(item_model)
```