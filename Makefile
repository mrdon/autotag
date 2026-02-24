.PHONY: help venv rebuild-index lint format lint-py lint-js format-py format-js check-format-py

# Help system from https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.DEFAULT_GOAL := help

help:
	@grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

venv: ## Create virtual environment and install dependencies
	cd sigal && rm -rf venv && uv venv venv && uv pip install -r requirements.txt --python venv/bin/python

build: ## Builds the website using sigal
	cd sigal && venv/bin/sigal build ../web/content/galleries public

rebuild: ## Full rebuild, ignoring cache
	cd sigal && venv/bin/sigal build -f ../web/content/galleries public

run: build ## Runs the website using sigal
	cd sigal && venv/bin/sigal serve public
