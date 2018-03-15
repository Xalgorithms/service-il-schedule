require 'kafka'

require_relative '../lib/local_env'

module Services
  class Messages
    LOCAL_ENV = LocalEnv.new(
      'KAFKA', {
        topic:   { type: :string, default: 'xadf.compute.documents' },
        brokers: { type: :list,   default: ['localhost:9092'] },
        client:  { type: :string, default: 'xadf_services_schedule' },
      })

    def initialize()
      topic = LOCAL_ENV.get(:topic)
      brokers = LOCAL_ENV.get(:brokers)
      client = LOCAL_ENV.get(:client)
      
      puts "> connecting to kafka (brokers=#{brokers}; client=#{client})"
      @kafka = Kafka.new(
        seed_brokers: brokers,
        # Set an optional client id in order to identify the client to Kafka:
        client_id: client,
      )
      puts "< connected"
    end
    
    def deliver_document(id)
      with_producer do |producer|
        topic = LOCAL_ENV.get(:topic)
        puts "> deliver (topic=#{topic})"
        producer.produce(id, topic: topic)
      end
    end

    private

    def with_producer
      producer = @kafka.async_producer
      yield(producer)
      producer.deliver_messages
      producer.shutdown
    end
  end
end
