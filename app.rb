require 'sinatra'
require 'sinatra/json'
require 'sinatra/config_file'

require_relative './services/actions'
require_relative "./services/documents"
require_relative "./services/messages"
require_relative "./services/tables"

config_file 'config.yml'

documents = Services::Documents.new(settings.mongo)
messages = Services::Messages.new(settings.kafka)
tables = Services::Tables.new(settings.cassandra)
actions = Services::Actions.new(documents, messages, tables)

before do
  content_type 'application/json'
end

helpers do
  def json_params
    begin
      JSON.parse(request.body.read)
    rescue
      halt 400, { message:'Invalid JSON' }.to_json
    end
  end
end

get '/status' do
  json(status: :live)
end

post '/actions' do
  data = json_params
  status = actions.execute(data["name"], data["payload"])
  json(status)
end
