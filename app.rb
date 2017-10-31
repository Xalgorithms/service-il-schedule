require 'sinatra'
require 'sinatra/json'
require 'sinatra/config_file'
require 'cassandra'

require_relative "./services/document"

config_file 'config.yml'
cluster = Cassandra.cluster(
  hosts: settings.db_hosts,
  port: settings.db_port
)
$session  = cluster.connect(settings.db_keyspace)

$kafka = Kafka.new(
  seed_brokers: settings.kafka_broker_hosts,
  # Set an optional client id in order to identify the client to Kafka:
  client_id: settings.kafka_client
)

generator = Cassandra::Uuid::Generator.new

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
  action = data["name"]
  payload = data["payload"]

  case action
  when 'document-add'
    id = generator.uuid
    resp = add_document id, payload
    json(resp)
  else
    halt 400, { message:'Unknown action' }.to_json
  end
end
