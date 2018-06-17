Rails.application.routes.draw do
  # Can't use subdomains with Heroku :(
  # constraints subdomain: "api" do
  scope module: "api", defaults: { format: :json } do
    namespace :v1 do
      resources :events, only: %i[index create update destroy]
    end
  end
  # end
end
