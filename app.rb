require 'sinatra'
require 'sinatra/json'

get '/status' do
  json(status: :live)
end


