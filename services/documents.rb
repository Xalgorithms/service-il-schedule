require 'mongo'
require 'uuid'

require_relative '../lib/local_env'

module Services
  class Documents
    LOCAL_ENV = LocalEnv.new(
      'MONGO', {
        url: { type: :string, default: 'mongodb://127.0.0.1:27017/xadf' },
      })

    def initialize()
      url = LOCAL_ENV.get(:url)

      puts "> connecting to Mongo (url=#{url})"
      @cl = Mongo::Client.new(url)
      puts "< connected"
    end

    def store(doc)
      id = UUID.generate
      res = @cl[:documents].insert_one(doc.merge(public_id: id))
      id
    end
  end
end
