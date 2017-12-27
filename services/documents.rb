require 'mongo'
require 'uuid'

module Services
  class Documents
    def initialize(opts)
      @cl = Mongo::Client.new(opts['url'])
    end
    
    def store(doc)
      id = UUID.generate
      res = @cl[:documents].insert_one(public_id: id, content: doc)
      id
    end
  end
end
